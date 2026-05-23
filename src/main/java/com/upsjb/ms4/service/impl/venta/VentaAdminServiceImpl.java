package com.upsjb.ms4.service.impl.venta;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.EstadoVenta;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.policy.VentaPolicy;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.venta.VentaAdminService;
import com.upsjb.ms4.service.contract.venta.VentaConsultaService;
import com.upsjb.ms4.service.contract.venta.VentaStockCommandService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.validator.VentaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class VentaAdminServiceImpl implements VentaAdminService {

    private static final Logger log = LoggerFactory.getLogger(VentaAdminServiceImpl.class);

    private static final String RECURSO_VENTA = "Venta";
    private static final String ENTIDAD_VENTA = "VENTA";
    private static final String ACCION_ANULAR_VENTA_ADMIN = "VENTA_ADMIN_ANULADA";

    private static final Set<EstadoVenta> ESTADOS_CON_IMPACTO_STOCK = Set.of(
            EstadoVenta.PAGADA,
            EstadoVenta.CONFIRMADA,
            EstadoVenta.PENDIENTE_SYNC_STOCK,
            EstadoVenta.ERROR_STOCK
    );

    private final VentaRepository ventaRepository;
    private final VentaConsultaService ventaConsultaService;
    private final VentaStockCommandService ventaStockCommandService;
    private final VentaPolicy ventaPolicy;
    private final VentaValidator ventaValidator;
    private final AuditoriaFuncionalService auditoriaFuncionalService;

    public VentaAdminServiceImpl(VentaRepository ventaRepository,
                                 VentaConsultaService ventaConsultaService,
                                 VentaStockCommandService ventaStockCommandService,
                                 VentaPolicy ventaPolicy,
                                 VentaValidator ventaValidator,
                                 AuditoriaFuncionalService auditoriaFuncionalService) {
        this.ventaRepository = ventaRepository;
        this.ventaConsultaService = ventaConsultaService;
        this.ventaStockCommandService = ventaStockCommandService;
        this.ventaPolicy = ventaPolicy;
        this.ventaValidator = ventaValidator;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
    }

    @Override
    @Transactional
    public VentaDetailResponseDto anularVenta(Long idVenta,
                                              EstadoChangeRequestDto request,
                                              AuthenticatedUserContext actor) {
        try {
            validarSolicitudAnulacion(request);

            Venta venta = resolverVentaActiva(idVenta);
            ventaPolicy.authorizeAnularVenta(actor, venta);
            ventaValidator.validarVentaAnulable(venta);

            EstadoVenta estadoAnterior = venta.getEstadoVenta();

            venta.setEstadoVenta(EstadoVenta.ANULADA);
            venta.setObservacion(anexarMotivoAnulacion(venta.getObservacion(), request.motivo()));
            ventaRepository.save(venta);

            if (requiereComandoAnulacionStock(estadoAnterior)) {
                ventaStockCommandService.registrarComandosAnulacionStock(venta, actor);
            }

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_VENTA,
                    venta.getId(),
                    ACCION_ANULAR_VENTA_ADMIN,
                    actor,
                    Map.of(
                            "codigoVenta", safe(venta.getCodigoVenta()),
                            "canalVenta", String.valueOf(venta.getCanalVenta()),
                            "estadoAnterior", String.valueOf(estadoAnterior),
                            "estadoNuevo", String.valueOf(venta.getEstadoVenta()),
                            "motivo", safe(request.motivo())
                    )
            );

            return ventaConsultaService.obtenerDetalleAdmin(venta.getId(), actor);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_ANULAR_VENTA_ADMIN,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico anulando venta desde administración. idVenta={}, actorIdUsuarioMs1={}",
                    idVenta,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_VENTA,
                    idVenta,
                    ACCION_ANULAR_VENTA_ADMIN,
                    actor,
                    ex
            );
            throw new BusinessException(
                    ErrorCodes.INTERNAL_ERROR,
                    "No se pudo anular la venta.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ex
            );
        }
    }

    private void validarSolicitudAnulacion(EstadoChangeRequestDto request) {
        if (request == null) {
            throw new ValidationException("La solicitud de anulación es obligatoria.");
        }

        if (!Boolean.FALSE.equals(request.estado())) {
            throw new ValidationException("Para anular una venta, el campo estado debe ser false.");
        }
    }

    private Venta resolverVentaActiva(Long idVenta) {
        if (idVenta == null || idVenta <= 0) {
            throw new ValidationException("El id de venta debe ser positivo.");
        }

        return ventaRepository.findByIdAndEstadoTrue(idVenta)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_VENTA, idVenta));
    }

    private boolean requiereComandoAnulacionStock(EstadoVenta estadoAnterior) {
        return estadoAnterior != null && ESTADOS_CON_IMPACTO_STOCK.contains(estadoAnterior);
    }

    private String anexarMotivoAnulacion(String observacionActual, String motivo) {
        String motivoNormalizado = safe(motivo);
        String bloque = "Anulación administrativa: " + motivoNormalizado;

        if (observacionActual == null || observacionActual.isBlank()) {
            return truncar(bloque, 500);
        }

        return truncar(observacionActual.trim() + " | " + bloque, 500);
    }

    private String truncar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
// ruta: src/main/java/com/upsjb/ms4/service/impl/caja/CajaMovimientoServiceImpl.java
package com.upsjb.ms4.service.impl.caja;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.caja.CajaMovimiento;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.enums.MetodoPago;
import com.upsjb.ms4.domain.enums.TipoMovimientoCaja;
import com.upsjb.ms4.dto.caja.filter.CajaMovimientoFilterDto;
import com.upsjb.ms4.dto.caja.request.CajaAjusteRequestDto;
import com.upsjb.ms4.dto.caja.response.CajaMovimientoResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.caja.CajaMovimientoMapper;
import com.upsjb.ms4.policy.CajaPolicy;
import com.upsjb.ms4.repository.CajaMovimientoRepository;
import com.upsjb.ms4.repository.CajaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.security.principal.AuthenticatedUserResolver;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.caja.CajaMovimientoService;
import com.upsjb.ms4.shared.audit.AuditContextHolder;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.CajaMovimientoSpecification;
import com.upsjb.ms4.util.MoneyUtil;
import com.upsjb.ms4.validator.CajaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CajaMovimientoServiceImpl implements CajaMovimientoService {

    private static final Logger log = LoggerFactory.getLogger(CajaMovimientoServiceImpl.class);

    private static final String RECURSO_CAJA = "Caja";
    private static final String ENTIDAD_CAJA_MOVIMIENTO = "CAJA_MOVIMIENTO";

    private final CajaMovimientoRepository cajaMovimientoRepository;
    private final CajaRepository cajaRepository;
    private final CajaMovimientoMapper cajaMovimientoMapper;
    private final CajaValidator cajaValidator;
    private final CajaPolicy cajaPolicy;
    private final PaginationService paginationService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AuditoriaFuncionalService auditoriaFuncionalService;

    public CajaMovimientoServiceImpl(CajaMovimientoRepository cajaMovimientoRepository,
                                     CajaRepository cajaRepository,
                                     CajaMovimientoMapper cajaMovimientoMapper,
                                     CajaValidator cajaValidator,
                                     CajaPolicy cajaPolicy,
                                     PaginationService paginationService,
                                     AuthenticatedUserResolver authenticatedUserResolver,
                                     AuditoriaFuncionalService auditoriaFuncionalService) {
        this.cajaMovimientoRepository = cajaMovimientoRepository;
        this.cajaRepository = cajaRepository;
        this.cajaMovimientoMapper = cajaMovimientoMapper;
        this.cajaValidator = cajaValidator;
        this.cajaPolicy = cajaPolicy;
        this.paginationService = paginationService;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
    }

    @Override
    @Transactional
    public CajaMovimientoResponseDto registrarMovimientoApertura(Caja caja, AuthenticatedUserContext actor) {
        try {
            cajaPolicy.authorizeRegistrarMovimientoCaja(actor, caja);
            cajaValidator.validarMovimientoApertura(caja);

            CajaMovimiento movimiento = construirMovimiento(
                    caja,
                    null,
                    null,
                    TipoMovimientoCaja.APERTURA,
                    MoneyUtil.money(caja.getMontoInicial()),
                    "Apertura de caja " + caja.getCodigoCaja(),
                    actor
            );

            movimiento = cajaMovimientoRepository.save(movimiento);
            registrarAuditoriaExito("REGISTRAR_MOVIMIENTO_APERTURA", movimiento, actor);

            return cajaMovimientoMapper.toResponse(movimiento);
        } catch (BusinessException ex) {
            registrarAuditoriaErrorUsuario("REGISTRAR_MOVIMIENTO_APERTURA", caja, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando movimiento de apertura. idCaja={}, actorIdUsuarioMs1={}",
                    caja == null ? null : caja.getId(),
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            registrarAuditoriaErrorTecnico("REGISTRAR_MOVIMIENTO_APERTURA", caja, actor, ex);
            throw internalError("No se pudo registrar el movimiento de apertura de caja.", ex);
        }
    }

    @Override
    @Transactional
    public CajaMovimientoResponseDto registrarMovimientoVentaEfectivo(Caja caja,
                                                                      Venta venta,
                                                                      Pago pago,
                                                                      AuthenticatedUserContext actor) {
        return registrarMovimientoVenta(caja, venta, pago, MetodoPago.EFECTIVO, TipoMovimientoCaja.VENTA_EFECTIVO, actor);
    }

    @Override
    @Transactional
    public CajaMovimientoResponseDto registrarMovimientoVentaTarjeta(Caja caja,
                                                                     Venta venta,
                                                                     Pago pago,
                                                                     AuthenticatedUserContext actor) {
        return registrarMovimientoVenta(
                caja,
                venta,
                pago,
                MetodoPago.TARJETA_PRESENCIAL_STRIPE_SANDBOX,
                TipoMovimientoCaja.VENTA_TARJETA,
                actor
        );
    }

    @Override
    @Transactional
    public CajaMovimientoResponseDto registrarMovimientoCierre(Caja caja, AuthenticatedUserContext actor) {
        try {
            cajaPolicy.authorizeRegistrarMovimientoCaja(actor, caja);
            cajaValidator.validarMovimientoCierre(caja);

            CajaMovimiento movimiento = construirMovimiento(
                    caja,
                    null,
                    null,
                    TipoMovimientoCaja.CIERRE,
                    MoneyUtil.money(caja.getMontoRealEfectivo()),
                    "Cierre de caja " + caja.getCodigoCaja(),
                    actor
            );

            movimiento = cajaMovimientoRepository.save(movimiento);
            registrarAuditoriaExito("REGISTRAR_MOVIMIENTO_CIERRE", movimiento, actor);

            return cajaMovimientoMapper.toResponse(movimiento);
        } catch (BusinessException ex) {
            registrarAuditoriaErrorUsuario("REGISTRAR_MOVIMIENTO_CIERRE", caja, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando movimiento de cierre. idCaja={}, actorIdUsuarioMs1={}",
                    caja == null ? null : caja.getId(),
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            registrarAuditoriaErrorTecnico("REGISTRAR_MOVIMIENTO_CIERRE", caja, actor, ex);
            throw internalError("No se pudo registrar el movimiento de cierre de caja.", ex);
        }
    }

    @Override
    @Transactional
    public CajaMovimientoResponseDto registrarMovimientoAjuste(Caja caja,
                                                               CajaAjusteRequestDto request,
                                                               AuthenticatedUserContext actor) {
        try {
            cajaPolicy.authorizeRegistrarMovimientoCaja(actor, caja);
            cajaValidator.validarAjuste(caja, request);

            CajaMovimiento movimiento = construirMovimiento(
                    caja,
                    null,
                    null,
                    TipoMovimientoCaja.AJUSTE,
                    MoneyUtil.money(request.monto()),
                    request.descripcion(),
                    actor
            );

            movimiento = cajaMovimientoRepository.save(movimiento);
            registrarAuditoriaExito("REGISTRAR_MOVIMIENTO_AJUSTE", movimiento, actor);

            return cajaMovimientoMapper.toResponse(movimiento);
        } catch (BusinessException ex) {
            registrarAuditoriaErrorUsuario("REGISTRAR_MOVIMIENTO_AJUSTE", caja, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando movimiento de ajuste. idCaja={}, actorIdUsuarioMs1={}",
                    caja == null ? null : caja.getId(),
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            registrarAuditoriaErrorTecnico("REGISTRAR_MOVIMIENTO_AJUSTE", caja, actor, ex);
            throw internalError("No se pudo registrar el ajuste de caja.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CajaMovimientoResponseDto> listarMovimientos(Long idCaja,
                                                                        CajaMovimientoFilterDto filter,
                                                                        PageRequestDto page) {
        AuthenticatedUserContext actor = authenticatedUserResolver.current();

        cajaValidator.validarFiltroMovimiento(idCaja, filter);
        Caja caja = cajaRepository.findById(idCaja)
                .filter(Caja::isActivo)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_CAJA, idCaja));

        cajaPolicy.authorizeListarMovimientosCaja(actor, caja);

        Specification<CajaMovimiento> spec = porCaja(idCaja)
                .and(CajaMovimientoSpecification.build(filter));

        Page<CajaMovimiento> movimientos = cajaMovimientoRepository.findAll(
                spec,
                paginationService.toPageable(page, "createdAt")
        );

        return paginationService.toPageResponse(movimientos, cajaMovimientoMapper::toResponse);
    }

    private CajaMovimientoResponseDto registrarMovimientoVenta(Caja caja,
                                                               Venta venta,
                                                               Pago pago,
                                                               MetodoPago metodoEsperado,
                                                               TipoMovimientoCaja tipoMovimiento,
                                                               AuthenticatedUserContext actor) {
        try {
            cajaPolicy.authorizeRegistrarMovimientoCaja(actor, caja);
            cajaValidator.validarMovimientoVenta(caja, venta, pago, metodoEsperado);

            CajaMovimiento movimiento = construirMovimiento(
                    caja,
                    venta,
                    pago,
                    tipoMovimiento,
                    MoneyUtil.money(pago.getMonto()),
                    construirDescripcionVenta(venta, pago, tipoMovimiento),
                    actor
            );

            movimiento = cajaMovimientoRepository.save(movimiento);
            registrarAuditoriaExito("REGISTRAR_MOVIMIENTO_" + tipoMovimiento.getCode(), movimiento, actor);

            return cajaMovimientoMapper.toResponse(movimiento);
        } catch (BusinessException ex) {
            registrarAuditoriaErrorUsuario("REGISTRAR_MOVIMIENTO_" + tipoMovimiento.getCode(), caja, actor, ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando movimiento de venta. idCaja={}, idVenta={}, idPago={}, tipoMovimiento={}, actorIdUsuarioMs1={}",
                    caja == null ? null : caja.getId(),
                    venta == null ? null : venta.getId(),
                    pago == null ? null : pago.getId(),
                    tipoMovimiento,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            registrarAuditoriaErrorTecnico("REGISTRAR_MOVIMIENTO_" + tipoMovimiento.getCode(), caja, actor, ex);
            throw internalError("No se pudo registrar el movimiento de venta en caja.", ex);
        }
    }

    private CajaMovimiento construirMovimiento(Caja caja,
                                               Venta venta,
                                               Pago pago,
                                               TipoMovimientoCaja tipoMovimiento,
                                               BigDecimal monto,
                                               String descripcion,
                                               AuthenticatedUserContext actor) {
        AuditContextHolder.AuditContext auditContext = AuditContextHolder.getOrEmpty();

        return CajaMovimiento.builder()
                .idCaja(caja.getId())
                .idVenta(venta == null ? null : venta.getId())
                .idPago(pago == null ? null : pago.getId())
                .tipoMovimiento(tipoMovimiento)
                .monto(MoneyUtil.money(monto))
                .descripcion(normalize(descripcion))
                .actorIdUsuarioMs1(actor.idUsuarioMs1())
                .actorRol(actor.rol())
                .requestId(auditContext.requestId())
                .correlationId(auditContext.correlationId())
                .estado(true)
                .build();
    }

    private Specification<CajaMovimiento> porCaja(Long idCaja) {
        return (root, query, cb) -> cb.equal(root.get("idCaja"), idCaja);
    }

    private String construirDescripcionVenta(Venta venta, Pago pago, TipoMovimientoCaja tipoMovimiento) {
        String codigoVenta = venta == null ? null : venta.getCodigoVenta();
        String codigoPago = pago == null ? null : pago.getCodigoPago();
        return tipoMovimiento.getLabel()
                + (codigoVenta == null ? "" : " - Venta " + codigoVenta)
                + (codigoPago == null ? "" : " - Pago " + codigoPago);
    }

    private void registrarAuditoriaExito(String accion, CajaMovimiento movimiento, AuthenticatedUserContext actor) {
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("idCaja", movimiento.getIdCaja());
        detalle.put("idVenta", movimiento.getIdVenta());
        detalle.put("idPago", movimiento.getIdPago());
        detalle.put("tipoMovimiento", movimiento.getTipoMovimiento().getCode());
        detalle.put("monto", movimiento.getMonto());

        auditoriaFuncionalService.registrarExito(
                ENTIDAD_CAJA_MOVIMIENTO,
                movimiento.getId(),
                accion,
                actor,
                detalle
        );
    }

    private void registrarAuditoriaErrorUsuario(String accion,
                                                Caja caja,
                                                AuthenticatedUserContext actor,
                                                BusinessException ex) {
        auditoriaFuncionalService.registrarErrorUsuario(
                ENTIDAD_CAJA_MOVIMIENTO,
                caja == null ? null : caja.getId(),
                accion,
                actor,
                ex.getCode(),
                ex.getMessage()
        );
    }

    private void registrarAuditoriaErrorTecnico(String accion,
                                                Caja caja,
                                                AuthenticatedUserContext actor,
                                                RuntimeException ex) {
        auditoriaFuncionalService.registrarErrorTecnico(
                ENTIDAD_CAJA_MOVIMIENTO,
                caja == null ? null : caja.getId(),
                accion,
                actor,
                ex
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(
                ErrorCodes.INTERNAL_ERROR,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
        );
    }
}
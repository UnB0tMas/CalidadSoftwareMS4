// ruta: src/main/java/com/upsjb/ms4/service/impl/venta/VentaStockCommandServiceImpl.java
package com.upsjb.ms4.service.impl.venta;

import com.upsjb.ms4.domain.entity.kafka.InventarioEventoPendienteMs4;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandEventDto;
import com.upsjb.ms4.mapper.kafka.StockCommandEventMapper;
import com.upsjb.ms4.repository.VentaDetalleRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.kafka.EventoDominioOutboxService;
import com.upsjb.ms4.service.contract.kafka.InventarioEventoPendienteService;
import com.upsjb.ms4.service.contract.reference.Ms4ReferenceResolverService;
import com.upsjb.ms4.service.contract.venta.VentaStockCommandService;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VentaStockCommandServiceImpl implements VentaStockCommandService {

    private static final Logger log = LoggerFactory.getLogger(VentaStockCommandServiceImpl.class);
    private static final String SERVICIO_MS3 = "MS3";

    private final VentaDetalleRepository ventaDetalleRepository;
    private final EventoDominioOutboxService outboxService;
    private final InventarioEventoPendienteService inventarioEventoPendienteService;
    private final Ms4ReferenceResolverService referenceResolver;
    private final StockCommandEventMapper stockCommandEventMapper;

    public VentaStockCommandServiceImpl(VentaDetalleRepository ventaDetalleRepository,
                                        EventoDominioOutboxService outboxService,
                                        InventarioEventoPendienteService inventarioEventoPendienteService,
                                        Ms4ReferenceResolverService referenceResolver,
                                        StockCommandEventMapper stockCommandEventMapper) {
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.outboxService = outboxService;
        this.inventarioEventoPendienteService = inventarioEventoPendienteService;
        this.referenceResolver = referenceResolver;
        this.stockCommandEventMapper = stockCommandEventMapper;
    }

    @Override
    @Transactional
    public void registrarComandosConfirmacionStock(Venta venta, AuthenticatedUserContext actor) {
        registrarComandos(venta, TipoComandoStock.CONFIRMAR_VENTA, actor);
    }

    @Override
    @Transactional
    public void registrarComandosAnulacionStock(Venta venta, AuthenticatedUserContext actor) {
        registrarComandos(venta, TipoComandoStock.ANULAR_VENTA, actor);
    }

    @Override
    @Transactional
    public void registrarComandoLiberacionReserva(Venta venta, AuthenticatedUserContext actor) {
        registrarComandos(venta, TipoComandoStock.LIBERAR_RESERVA, actor);
    }

    @Override
    public Ms4StockCommandEventDto construirComandoStock(Venta venta,
                                                         VentaDetalle detalle,
                                                         TipoComandoStock tipoComando) {
        return stockCommandEventMapper.toStockCommandEvent(
                venta,
                detalle,
                tipoComando,
                generarIdempotencyKey(venta, detalle, tipoComando)
        );
    }

    @Override
    public String generarIdempotencyKey(Venta venta,
                                        VentaDetalle detalle,
                                        TipoComandoStock tipoComando) {
        if (venta == null || detalle == null || tipoComando == null) {
            throw new ValidationException("Venta, detalle y tipo de comando son obligatorios para generar idempotencyKey.");
        }

        String idVenta = venta.getId() == null ? venta.getCodigoVenta() : venta.getId().toString();

        if (idVenta == null || idVenta.isBlank()) {
            throw new ValidationException("La venta debe tener id o código para generar idempotencyKey.");
        }

        if (detalle.getIdSkuMs3() == null || detalle.getIdSkuMs3() <= 0) {
            throw new ValidationException("El detalle debe tener SKU MS3 válido para generar idempotencyKey.");
        }

        return "MS4-VENTA-" + idVenta.trim() + "-SKU-" + detalle.getIdSkuMs3() + "-" + tipoComando.getCode();
    }

    @Override
    @Transactional
    public void registrarEventoPendienteSiContingencia(Venta venta,
                                                       VentaDetalle detalle,
                                                       TipoComandoStock tipoComando) {
        try {
            referenceResolver.resolverContingenciaActiva(SERVICIO_MS3);
            Ms4StockCommandEventDto event = construirComandoStock(venta, detalle, tipoComando);
            String payloadJson = stockCommandEventMapper.toOutboxPayload(event);
            inventarioEventoPendienteService.registrarPendiente(venta, detalle, tipoComando, payloadJson);
        } catch (NotFoundException ignored) {
            return;
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico registrando evento pendiente por contingencia. idVenta={}, idDetalle={}, tipo={}",
                    venta == null ? null : venta.getId(),
                    detalle == null ? null : detalle.getId(),
                    tipoComando,
                    ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void registrarOutboxStockCommand(Ms4StockCommandEventDto event,
                                            AuthenticatedUserContext actor) {
        outboxService.crearEventoStockCommand(event, actor);
    }

    private void registrarComandos(Venta venta,
                                   TipoComandoStock tipoComando,
                                   AuthenticatedUserContext actor) {
        if (venta == null || venta.getId() == null) {
            throw new ValidationException("La venta persistida es obligatoria para registrar comandos de stock.");
        }

        List<VentaDetalle> detalles = ventaDetalleRepository.findByIdVentaAndEstadoTrueOrderByIdAsc(venta.getId());

        if (detalles.isEmpty()) {
            throw new ValidationException("La venta no tiene detalles activos para registrar comandos de stock.");
        }

        for (VentaDetalle detalle : detalles) {
            Ms4StockCommandEventDto event = construirComandoStock(venta, detalle, tipoComando);
            registrarOutboxStockCommand(event, actor);
            registrarEventoPendienteSiContingencia(venta, detalle, tipoComando);
        }
    }
}
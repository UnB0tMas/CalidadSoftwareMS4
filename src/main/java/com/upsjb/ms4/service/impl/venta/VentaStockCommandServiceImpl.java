package com.upsjb.ms4.service.impl.venta;

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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaStockCommandServiceImpl
        implements VentaStockCommandService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    VentaStockCommandServiceImpl.class
            );

    private static final String SERVICIO_MS3 =
            "MS3";

    private final VentaDetalleRepository ventaDetalleRepository;
    private final EventoDominioOutboxService outboxService;
    private final InventarioEventoPendienteService
            inventarioEventoPendienteService;
    private final Ms4ReferenceResolverService referenceResolver;
    private final StockCommandEventMapper stockCommandEventMapper;

    public VentaStockCommandServiceImpl(
            VentaDetalleRepository ventaDetalleRepository,
            EventoDominioOutboxService outboxService,
            InventarioEventoPendienteService
                    inventarioEventoPendienteService,
            Ms4ReferenceResolverService referenceResolver,
            StockCommandEventMapper stockCommandEventMapper
    ) {
        this.ventaDetalleRepository =
                ventaDetalleRepository;
        this.outboxService =
                outboxService;
        this.inventarioEventoPendienteService =
                inventarioEventoPendienteService;
        this.referenceResolver =
                referenceResolver;
        this.stockCommandEventMapper =
                stockCommandEventMapper;
    }

    @Override
    @Transactional
    public void registrarComandosConfirmacionStock(
            Venta venta,
            AuthenticatedUserContext actor
    ) {
        /*
         * MS3 confirma una reserva existente.
         *
         * Para cada detalle se crea primero RESERVAR_STOCK y después
         * CONFIRMAR_VENTA.
         *
         * Los dos eventos usan la misma key Kafka:
         *
         * STOCK_STREAM:<idSkuMs3>:<idAlmacenMs3>
         *
         * El repositorio Outbox impide reclamar CONFIRMAR_VENTA hasta
         * que RESERVAR_STOCK se encuentre PUBLICADO.
         */
        registrarComandos(
                venta,
                List.of(
                        TipoComandoStock.RESERVAR_STOCK,
                        TipoComandoStock.CONFIRMAR_VENTA
                ),
                actor
        );
    }

    @Override
    @Transactional
    public void registrarComandosAnulacionStock(
            Venta venta,
            AuthenticatedUserContext actor
    ) {
        registrarComandos(
                venta,
                List.of(
                        TipoComandoStock.ANULAR_VENTA
                ),
                actor
        );
    }

    @Override
    @Transactional
    public void registrarComandoLiberacionReserva(
            Venta venta,
            AuthenticatedUserContext actor
    ) {
        registrarComandos(
                venta,
                List.of(
                        TipoComandoStock.LIBERAR_RESERVA
                ),
                actor
        );
    }

    @Override
    public Ms4StockCommandEventDto construirComandoStock(
            Venta venta,
            VentaDetalle detalle,
            TipoComandoStock tipoComando
    ) {
        return stockCommandEventMapper
                .toStockCommandEvent(
                        venta,
                        detalle,
                        tipoComando,
                        generarIdempotencyKey(
                                venta,
                                detalle,
                                tipoComando
                        )
                );
    }

    @Override
    public String generarIdempotencyKey(
            Venta venta,
            VentaDetalle detalle,
            TipoComandoStock tipoComando
    ) {
        if (
                venta == null
                        || detalle == null
                        || tipoComando == null
        ) {
            throw new ValidationException(
                    "Venta, detalle y tipo de comando son obligatorios para generar la idempotencyKey."
            );
        }

        if (
                venta.getId() == null
                        || venta.getId() <= 0
        ) {
            throw new ValidationException(
                    "La venta debe estar persistida para generar la idempotencyKey."
            );
        }

        if (
                detalle.getId() == null
                        || detalle.getId() <= 0
        ) {
            throw new ValidationException(
                    "El detalle de venta debe estar persistido para generar la idempotencyKey."
            );
        }

        if (
                detalle.getIdSkuMs3() == null
                        || detalle.getIdSkuMs3() <= 0
        ) {
            throw new ValidationException(
                    "El detalle debe tener un SKU MS3 válido para generar la idempotencyKey."
            );
        }

        if (
                detalle.getIdAlmacenMs3() == null
                        || detalle.getIdAlmacenMs3() <= 0
        ) {
            throw new ValidationException(
                    "El detalle debe tener un almacén MS3 válido para generar la idempotencyKey."
            );
        }

        if (!tipoComando.soportadoPorMs3StockCommand()) {
            throw new ValidationException(
                    "El tipo de comando no está soportado por ms4.stock.command.v1."
            );
        }

        return "MS4-VENTA-"
                + venta.getId()
                + "-DET-"
                + detalle.getId()
                + "-SKU-"
                + detalle.getIdSkuMs3()
                + "-ALM-"
                + detalle.getIdAlmacenMs3()
                + "-"
                + tipoComando.getCode();
    }

    @Override
    @Transactional
    public void registrarEventoPendienteSiContingencia(
            Venta venta,
            VentaDetalle detalle,
            TipoComandoStock tipoComando
    ) {
        Ms4StockCommandEventDto event =
                construirComandoStock(
                        venta,
                        detalle,
                        tipoComando
                );

        registrarEventoPendienteSiContingencia(
                venta,
                detalle,
                tipoComando,
                event
        );
    }

    @Override
    @Transactional
    public void registrarOutboxStockCommand(
            Ms4StockCommandEventDto event,
            AuthenticatedUserContext actor
    ) {
        outboxService.crearEventoStockCommand(
                event,
                actor
        );
    }

    private void registrarComandos(
            Venta venta,
            List<TipoComandoStock> tiposComando,
            AuthenticatedUserContext actor
    ) {
        validarVentaPersistida(venta);

        if (
                tiposComando == null
                        || tiposComando.isEmpty()
        ) {
            throw new ValidationException(
                    "Debe indicar al menos un tipo de comando de stock."
            );
        }

        List<VentaDetalle> detalles =
                ventaDetalleRepository
                        .findByIdVentaAndEstadoTrueOrderByIdAsc(
                                venta.getId()
                        );

        if (detalles.isEmpty()) {
            throw new ValidationException(
                    "La venta no tiene detalles activos para registrar comandos de stock."
            );
        }

        /*
         * El detalle es el bucle exterior para garantizar que los eventos
         * RESERVAR y CONFIRMAR del mismo SKU/almacén sean creados de forma
         * consecutiva en la tabla Outbox.
         */
        for (VentaDetalle detalle : detalles) {
            for (
                    TipoComandoStock tipoComando
                    : tiposComando
            ) {
                Ms4StockCommandEventDto event =
                        construirComandoStock(
                                venta,
                                detalle,
                                tipoComando
                        );

                registrarOutboxStockCommand(
                        event,
                        actor
                );

                /*
                 * Se reutiliza exactamente el mismo evento.
                 *
                 * No debe construirse otro evento porque eso produciría
                 * un eventId distinto al guardado en Outbox.
                 */
                registrarEventoPendienteSiContingencia(
                        venta,
                        detalle,
                        tipoComando,
                        event
                );
            }
        }
    }

    private void registrarEventoPendienteSiContingencia(
            Venta venta,
            VentaDetalle detalle,
            TipoComandoStock tipoComando,
            Ms4StockCommandEventDto event
    ) {
        try {
            referenceResolver
                    .resolverContingenciaActiva(
                            SERVICIO_MS3
                    );

            String payloadJson =
                    stockCommandEventMapper
                            .toOutboxPayload(event);

            inventarioEventoPendienteService
                    .registrarPendiente(
                            venta,
                            detalle,
                            tipoComando,
                            payloadJson
                    );
        } catch (NotFoundException ignored) {
            /*
             * Cuando no existe una contingencia activa, el Outbox
             * transaccional es el único mecanismo requerido.
             */
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico registrando evento pendiente por contingencia. idVenta={}, idDetalle={}, tipo={}",
                    venta == null
                            ? null
                            : venta.getId(),
                    detalle == null
                            ? null
                            : detalle.getId(),
                    tipoComando,
                    ex
            );

            throw ex;
        }
    }

    private void validarVentaPersistida(
            Venta venta
    ) {
        if (
                venta == null
                        || venta.getId() == null
                        || venta.getId() <= 0
        ) {
            throw new ValidationException(
                    "La venta persistida es obligatoria para registrar comandos de stock."
            );
        }

        if (
                venta.getCodigoVenta() == null
                        || venta.getCodigoVenta()
                        .isBlank()
        ) {
            throw new ValidationException(
                    "La venta debe tener un código funcional para registrar comandos de stock."
            );
        }
    }
}
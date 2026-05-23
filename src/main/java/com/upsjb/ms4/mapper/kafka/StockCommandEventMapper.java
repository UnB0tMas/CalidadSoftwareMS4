// ruta: src/main/java/com/upsjb/ms4/mapper/kafka/StockCommandEventMapper.java
package com.upsjb.ms4.mapper.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4AlmacenPayloadDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4SkuPayloadDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandEventDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandPayloadDto;
import com.upsjb.ms4.dto.kafka.ms4.Ms4VentaPayloadDto;
import com.upsjb.ms4.shared.exception.KafkaPublishException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class StockCommandEventMapper {

    private static final String SOURCE_SERVICE = "ms-ventas-facturacion";
    private static final String AGGREGATE_TYPE = "STOCK";
    private static final String REFERENCIA_TIPO = "VENTA_MS4";

    private final ObjectMapper objectMapper;

    public StockCommandEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Ms4StockCommandEventDto toStockCommandEvent(
            Venta venta,
            VentaDetalle detalle,
            TipoComandoStock tipoComando,
            String idempotencyKey
    ) {
        validateBase(venta, detalle, tipoComando);

        UUID eventId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();

        String finalIdempotencyKey = normalizeIdempotencyKey(
                venta,
                detalle,
                tipoComando,
                idempotencyKey
        );

        Ms4StockCommandPayloadDto payload = toPayload(
                eventId,
                venta,
                detalle,
                tipoComando,
                finalIdempotencyKey,
                occurredAt
        );

        DomainEventEnvelopeDto<Ms4StockCommandPayloadDto> envelope = new DomainEventEnvelopeDto<>(
                eventId,
                tipoComando.ms3EnvelopeEventType(),
                SOURCE_SERVICE,
                AGGREGATE_TYPE,
                venta.getCodigoVenta().trim(),
                1,
                SOURCE_SERVICE,
                occurredAt,
                trimToNull(venta.getRequestId()),
                trimToNull(venta.getCorrelationId()),
                payload,
                Map.of(
                        "venta", new Ms4VentaPayloadDto(
                                venta.getId(),
                                venta.getCodigoVenta(),
                                venta.getCanalVenta()
                        ),
                        "idVentaDetalleMs4", detalle.getId() == null ? "" : detalle.getId().toString(),
                        "tipoComandoMs4", tipoComando.getCode()
                )
        );

        return new Ms4StockCommandEventDto(envelope);
    }

    public Ms4StockCommandPayloadDto toPayload(
            UUID eventId,
            Venta venta,
            VentaDetalle detalle,
            TipoComandoStock tipoComando,
            String idempotencyKey,
            LocalDateTime occurredAt
    ) {
        validateBase(venta, detalle, tipoComando);

        return new Ms4StockCommandPayloadDto(
                eventId.toString(),
                idempotencyKey,
                tipoComando.ms3PayloadEventType(),
                new Ms4SkuPayloadDto(
                        detalle.getIdSkuMs3(),
                        trimToNull(detalle.getCodigoSku()),
                        trimToNull(detalle.getCodigoSku()),
                        null
                ),
                new Ms4AlmacenPayloadDto(
                        detalle.getIdAlmacenMs3(),
                        trimToNull(detalle.getCodigoAlmacen()),
                        trimToNull(detalle.getCodigoAlmacen()),
                        null
                ),
                REFERENCIA_TIPO,
                venta.getCodigoVenta().trim(),
                detalle.getCantidad(),
                null,
                resolveActorIdUsuarioMs1(venta),
                venta.getIdEmpleadoMs2(),
                resolveActorRol(venta),
                occurredAt,
                null,
                "Comando de stock generado por venta MS4 " + venta.getCodigoVenta().trim(),
                trimToNull(venta.getRequestId()),
                trimToNull(venta.getCorrelationId()),
                null
        );
    }

    public String toOutboxPayload(Ms4StockCommandEventDto event) {
        if (event == null || event.envelope() == null || event.payload() == null) {
            throw new KafkaPublishException("El evento de comando de stock MS4 es obligatorio.");
        }

        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new KafkaPublishException("No se pudo serializar el evento de comando de stock MS4.", ex);
        }
    }

    public String eventKey(Venta venta, VentaDetalle detalle) {
        if (venta == null || detalle == null) {
            return null;
        }

        String ventaKey = trimToNull(venta.getCodigoVenta());
        if (ventaKey == null) {
            ventaKey = venta.getId() == null ? "SIN_VENTA" : venta.getId().toString();
        }

        return "venta:" + ventaKey + ":sku:" + detalle.getIdSkuMs3();
    }

    private void validateBase(Venta venta, VentaDetalle detalle, TipoComandoStock tipoComando) {
        if (venta == null) {
            throw new KafkaPublishException("La venta es obligatoria para generar comando de stock.");
        }

        if (detalle == null) {
            throw new KafkaPublishException("El detalle de venta es obligatorio para generar comando de stock.");
        }

        if (tipoComando == null) {
            throw new KafkaPublishException("El tipo de comando de stock es obligatorio.");
        }

        if (!tipoComando.soportadoPorMs3StockCommand()) {
            throw new KafkaPublishException("El comando " + tipoComando.getCode() + " no está soportado por ms4.stock.command.v1.");
        }

        if (trimToNull(venta.getCodigoVenta()) == null) {
            throw new KafkaPublishException("La venta no tiene código funcional para referencia externa.");
        }

        if (detalle.getIdSkuMs3() == null || detalle.getIdSkuMs3() <= 0) {
            throw new KafkaPublishException("El detalle de venta no tiene SKU MS3 válido.");
        }

        if (detalle.getIdAlmacenMs3() == null || detalle.getIdAlmacenMs3() <= 0) {
            throw new KafkaPublishException("El detalle de venta no tiene almacén MS3 válido.");
        }

        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new KafkaPublishException("La cantidad del comando de stock debe ser positiva.");
        }
    }

    private String normalizeIdempotencyKey(
            Venta venta,
            VentaDetalle detalle,
            TipoComandoStock tipoComando,
            String idempotencyKey
    ) {
        String explicit = trimToNull(idempotencyKey);
        if (explicit != null) {
            return explicit;
        }

        String idVenta = venta.getId() == null ? venta.getCodigoVenta().trim() : venta.getId().toString();
        return "MS4-VENTA-" + idVenta + "-SKU-" + detalle.getIdSkuMs3() + "-" + tipoComando.getCode();
    }

    private Long resolveActorIdUsuarioMs1(Venta venta) {
        if (venta.getIdUsuarioEmpleadoMs1() != null) {
            return venta.getIdUsuarioEmpleadoMs1();
        }

        return venta.getIdUsuarioClienteMs1();
    }

    private String resolveActorRol(Venta venta) {
        if (venta.getIdUsuarioEmpleadoMs1() != null || venta.getIdEmpleadoMs2() != null) {
            return "EMPLEADO";
        }

        return "CLIENTE";
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
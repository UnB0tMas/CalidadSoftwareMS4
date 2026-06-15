package com.upsjb.ms4.kafka.probe;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class KafkaFunctionalProbeRegistry {

    private final AtomicReference<StockCandidate>
            stockCandidate =
            new AtomicReference<>();

    public void registerStockCandidate(
            StockCandidate candidate
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException(
                    "El candidato de stock funcional es obligatorio."
            );
        }

        stockCandidate.set(
                candidate
        );
    }

    public Optional<StockCandidate>
    currentStockCandidate() {
        return Optional.ofNullable(
                stockCandidate.get()
        );
    }

    public void clear() {
        stockCandidate.set(null);
    }

    public record StockCandidate(
            String sourceProbeId,
            UUID sourceEventId,
            Long idStockMs3,
            Long idSkuMs3,
            String codigoSku,
            String barcode,
            Long idProductoMs3,
            String codigoProducto,
            String nombreProducto,
            Long idAlmacenMs3,
            String codigoAlmacen,
            String nombreAlmacen,
            Integer stockFisico,
            Integer stockReservado,
            Integer stockDisponible
    ) {

        public StockCandidate {
            if (
                    idStockMs3 == null
                            || idStockMs3 <= 0
                            || idSkuMs3 == null
                            || idSkuMs3 <= 0
                            || idAlmacenMs3 == null
                            || idAlmacenMs3 <= 0
            ) {
                throw new IllegalArgumentException(
                        "El candidato funcional debe contener identificadores válidos de stock, SKU y almacén."
                );
            }

            if (
                    stockDisponible == null
                            || stockDisponible <= 0
            ) {
                throw new IllegalArgumentException(
                        "El candidato funcional debe tener stock disponible."
                );
            }
        }

        public String stockStreamKey() {
            return "STOCK_STREAM:"
                    + idSkuMs3
                    + ":"
                    + idAlmacenMs3;
        }
    }
}
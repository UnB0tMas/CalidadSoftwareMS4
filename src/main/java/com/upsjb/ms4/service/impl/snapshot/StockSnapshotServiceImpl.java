// ruta: src/main/java/com/upsjb/ms4/service/impl/snapshot/StockSnapshotServiceImpl.java
package com.upsjb.ms4.service.impl.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.StockSnapshotPayloadDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.StockVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.response.StockVentaResponseDto;
import com.upsjb.ms4.mapper.snapshot.StockSnapshotMapper;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.repository.StockSnapshotMs3Repository;
import com.upsjb.ms4.service.contract.snapshot.StockSnapshotService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.ConflictException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.StockSnapshotSpecification;
import com.upsjb.ms4.validator.SnapshotValidator;
import com.upsjb.ms4.validator.StockDisponibilidadValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockSnapshotServiceImpl implements StockSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(StockSnapshotServiceImpl.class);

    private static final String RECURSO = "Stock snapshot MS3";

    private final StockSnapshotMs3Repository repository;
    private final StockSnapshotMapper mapper;
    private final SnapshotValidator snapshotValidator;
    private final StockDisponibilidadValidator stockDisponibilidadValidator;
    private final SnapshotPolicy snapshotPolicy;
    private final PaginationService paginationService;

    public StockSnapshotServiceImpl(StockSnapshotMs3Repository repository,
                                    StockSnapshotMapper mapper,
                                    SnapshotValidator snapshotValidator,
                                    StockDisponibilidadValidator stockDisponibilidadValidator,
                                    SnapshotPolicy snapshotPolicy,
                                    PaginationService paginationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.snapshotValidator = snapshotValidator;
        this.stockDisponibilidadValidator = stockDisponibilidadValidator;
        this.snapshotPolicy = snapshotPolicy;
        this.paginationService = paginationService;
    }

    @Override
    @Transactional
    public void procesarStockSnapshot(DomainEventEnvelopeDto<StockSnapshotPayloadDto> envelope, String payloadJson) {
        try {
            snapshotPolicy.authorizeProcesarSnapshotKafka();
            validarEventoBase(envelope, payloadJson);
            StockSnapshotPayloadDto payload = envelope.payload();
            validarPayload(payload);

            if (repository.existsByEventId(envelope.eventId())) {
                log.debug("Snapshot stock MS3 ignorado por idempotencia. eventId={}", envelope.eventId());
                return;
            }

            StockSnapshotMs3 entity = repository.findByIdStockMs3(payload.idStock())
                    .orElseGet(StockSnapshotMs3::new);

            if (entity.getId() != null) {
                snapshotValidator.validarEventoNoObsoleto(envelope.occurredAt(), entity.getOccurredAt());
                snapshotValidator.validarVersionEvento(envelope.eventVersionSafe(), entity.getEventVersion());
                mapper.updateFromPayload(entity, payload, envelope, payloadJson);
            } else {
                entity = mapper.toEntityFromPayload(payload, envelope, payloadJson);
            }

            repository.save(entity);

            log.info("Snapshot stock MS3 procesado. idStockMs3={}, idSkuMs3={}, idAlmacenMs3={}, eventId={}",
                    payload.idStock(),
                    payload.idSku(),
                    payload.idAlmacen(),
                    envelope.eventId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico procesando snapshot stock MS3. eventId={}, aggregateId={}",
                    envelope == null ? null : envelope.eventId(),
                    envelope == null ? null : envelope.aggregateIdSafe(),
                    ex);
            throw internalError("No se pudo procesar el snapshot de stock MS3.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StockVentaResponseDto obtenerStock(Long idSkuMs3, Long idAlmacenMs3) {
        return mapper.toResponse(resolverStockDisponible(idSkuMs3, idAlmacenMs3));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<StockVentaResponseDto> listar(StockVentaFilterDto filter, PageRequestDto page) {
        Page<StockSnapshotMs3> result = repository.findAll(
                StockSnapshotSpecification.build(filter),
                paginationService.toPageable(page, "nombreAlmacen")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StockSnapshotMs3 resolverStockDisponible(Long idSkuMs3, Long idAlmacenMs3) {
        requirePositive(idSkuMs3, "El idSkuMs3");
        requirePositive(idAlmacenMs3, "El idAlmacenMs3");

        StockSnapshotMs3 stock = repository
                .findByIdSkuMs3AndIdAlmacenMs3AndEstadoTrue(idSkuMs3, idAlmacenMs3)
                .orElseThrow(() -> new NotFoundException(
                        "No existe stock activo para el SKU MS3 " + idSkuMs3
                                + " en el almacén MS3 " + idAlmacenMs3 + "."
                ));

        stockDisponibilidadValidator.validarAlmacenActivo(stock);

        if (stock.getStockDisponible() == null || stock.getStockDisponible() <= 0) {
            throw new ConflictException("No existe stock disponible para el SKU solicitado en el almacén indicado.");
        }

        return stock;
    }

    @Override
    @Transactional(readOnly = true)
    public void validarDisponibilidad(Long idSkuMs3, Long idAlmacenMs3, Integer cantidadSolicitada) {
        StockSnapshotMs3 stock = resolverStockDisponible(idSkuMs3, idAlmacenMs3);
        stockDisponibilidadValidator.validarStockDisponible(stock, cantidadSolicitada);
    }

    @Override
    @Transactional
    public void aplicarActualizacionLocalPorSnapshot(StockSnapshotPayloadDto payload, String payloadJson) {
        try {
            validarPayload(payload);
            snapshotValidator.validarPayloadJson(payloadJson);

            StockSnapshotMs3 entity = repository.findByIdStockMs3(payload.idStock())
                    .or(() -> repository.findByIdSkuMs3AndIdAlmacenMs3(payload.idSku(), payload.idAlmacen()))
                    .orElseThrow(() -> new NotFoundException(
                            "No existe stock snapshot local para aplicar actualización sin envelope Kafka."
                    ));

            entity.setIdStockMs3(payload.idStock());
            entity.setIdSkuMs3(payload.idSku());
            entity.setCodigoSku(trimToNull(payload.codigoSku()));
            entity.setBarcode(trimToNull(payload.barcode()));
            entity.setIdProductoMs3(payload.idProducto());
            entity.setCodigoProducto(trimToNull(payload.codigoProducto()));
            entity.setNombreProducto(trimToNull(payload.nombreProducto()));
            entity.setIdAlmacenMs3(payload.idAlmacen());
            entity.setCodigoAlmacen(trimToNull(payload.codigoAlmacen()));
            entity.setNombreAlmacen(trimToNull(payload.nombreAlmacen()));
            entity.setStockFisico(numberOrZero(payload.stockFisico()));
            entity.setStockReservado(numberOrZero(payload.stockReservado()));
            entity.setStockDisponible(numberOrZero(payload.stockDisponible()));
            entity.setStockMinimo(payload.stockMinimo());
            entity.setStockMaximo(payload.stockMaximo());
            entity.setCostoPromedioActual(payload.costoPromedioActual());
            entity.setUltimoCostoCompra(payload.ultimoCostoCompra());
            entity.setBajoStock(Boolean.TRUE.equals(payload.bajoStock()));
            entity.setSobreStock(Boolean.TRUE.equals(payload.sobreStock()));
            entity.setPayloadJson(payloadJson);
            entity.setEstado(payload.estado() == null || Boolean.TRUE.equals(payload.estado()));

            repository.save(entity);

            log.info("Actualización local de stock aplicada. idStockMs3={}, idSkuMs3={}, idAlmacenMs3={}",
                    payload.idStock(),
                    payload.idSku(),
                    payload.idAlmacen());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico aplicando actualización local de stock. idStockMs3={}, idSkuMs3={}, idAlmacenMs3={}",
                    payload == null ? null : payload.idStock(),
                    payload == null ? null : payload.idSku(),
                    payload == null ? null : payload.idAlmacen(),
                    ex);
            throw internalError("No se pudo aplicar la actualización local de stock.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneStockDisponible(Long idSkuMs3, Long idAlmacenMs3, Integer cantidadSolicitada) {
        try {
            validarDisponibilidad(idSkuMs3, idAlmacenMs3, cantidadSolicitada);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private void validarEventoBase(DomainEventEnvelopeDto<?> envelope, String payloadJson) {
        snapshotValidator.validarEnvelope(envelope);
        snapshotValidator.validarPayloadJson(payloadJson);
    }

    private void validarPayload(StockSnapshotPayloadDto payload) {
        if (payload == null) {
            throw new ValidationException("El payload del snapshot de stock MS3 es obligatorio.");
        }

        requirePositive(payload.idStock(), "El idStock MS3");
        requirePositive(payload.idSku(), "El idSku MS3");
        requirePositive(payload.idProducto(), "El idProducto MS3");
        requirePositive(payload.idAlmacen(), "El idAlmacen MS3");
        requireText(payload.codigoSku(), "El código SKU es obligatorio.");
        requireText(payload.codigoProducto(), "El código de producto es obligatorio.");
        requireText(payload.nombreProducto(), "El nombre del producto es obligatorio.");
        requireText(payload.codigoAlmacen(), "El código de almacén es obligatorio.");
        requireText(payload.nombreAlmacen(), "El nombre de almacén es obligatorio.");

        validarNoNegativo(payload.stockFisico(), "El stock físico");
        validarNoNegativo(payload.stockReservado(), "El stock reservado");
        validarNoNegativo(payload.stockDisponible(), "El stock disponible");
    }

    private void validarNoNegativo(Integer value, String fieldName) {
        if (value == null || value < 0) {
            throw new ValidationException(fieldName + " no puede ser negativo.");
        }
    }

    private void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ValidationException(fieldName + " debe ser un identificador positivo.");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    private Integer numberOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(ErrorCodes.INTERNAL_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}
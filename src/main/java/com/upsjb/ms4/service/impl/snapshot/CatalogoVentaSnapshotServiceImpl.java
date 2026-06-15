package com.upsjb.ms4.service.impl.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSkuDescuentoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.PromocionSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms3.PrecioSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.ProductoSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSkuDescuentoPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.PromocionSnapshotPayloadDto;
import com.upsjb.ms4.dto.kafka.ms3.SkuSnapshotPayloadDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ProductoVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.filter.SkuVentaFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ProductoVentaResponseDto;
import com.upsjb.ms4.dto.snapshot.response.SkuVentaResponseDto;
import com.upsjb.ms4.mapper.snapshot.PrecioSnapshotMapper;
import com.upsjb.ms4.mapper.snapshot.ProductoSnapshotMapper;
import com.upsjb.ms4.mapper.snapshot.PromocionSnapshotMapper;
import com.upsjb.ms4.mapper.snapshot.SkuSnapshotMapper;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.repository.PrecioSnapshotMs3Repository;
import com.upsjb.ms4.repository.ProductoSnapshotMs3Repository;
import com.upsjb.ms4.repository.PromocionSkuDescuentoSnapshotMs3Repository;
import com.upsjb.ms4.repository.PromocionSnapshotMs3Repository;
import com.upsjb.ms4.repository.SkuSnapshotMs3Repository;
import com.upsjb.ms4.service.contract.snapshot.CatalogoVentaSnapshotService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.ProductoSnapshotSpecification;
import com.upsjb.ms4.specification.SkuSnapshotSpecification;
import com.upsjb.ms4.validator.SnapshotValidator;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoVentaSnapshotServiceImpl
        implements CatalogoVentaSnapshotService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    CatalogoVentaSnapshotServiceImpl.class
            );

    private final ProductoSnapshotMs3Repository
            productoRepository;

    private final SkuSnapshotMs3Repository
            skuRepository;

    private final PrecioSnapshotMs3Repository
            precioRepository;

    private final PromocionSnapshotMs3Repository
            promocionRepository;

    private final PromocionSkuDescuentoSnapshotMs3Repository
            descuentoRepository;

    private final ProductoSnapshotMapper
            productoMapper;

    private final SkuSnapshotMapper
            skuMapper;

    private final PrecioSnapshotMapper
            precioMapper;

    private final PromocionSnapshotMapper
            promocionMapper;

    private final SnapshotValidator
            snapshotValidator;

    private final SnapshotPolicy
            snapshotPolicy;

    private final PaginationService
            paginationService;

    private final Clock clock;

    public CatalogoVentaSnapshotServiceImpl(
            ProductoSnapshotMs3Repository productoRepository,
            SkuSnapshotMs3Repository skuRepository,
            PrecioSnapshotMs3Repository precioRepository,
            PromocionSnapshotMs3Repository promocionRepository,
            PromocionSkuDescuentoSnapshotMs3Repository descuentoRepository,
            ProductoSnapshotMapper productoMapper,
            SkuSnapshotMapper skuMapper,
            PrecioSnapshotMapper precioMapper,
            PromocionSnapshotMapper promocionMapper,
            SnapshotValidator snapshotValidator,
            SnapshotPolicy snapshotPolicy,
            PaginationService paginationService,
            Clock clock
    ) {
        this.productoRepository =
                productoRepository;

        this.skuRepository =
                skuRepository;

        this.precioRepository =
                precioRepository;

        this.promocionRepository =
                promocionRepository;

        this.descuentoRepository =
                descuentoRepository;

        this.productoMapper =
                productoMapper;

        this.skuMapper =
                skuMapper;

        this.precioMapper =
                precioMapper;

        this.promocionMapper =
                promocionMapper;

        this.snapshotValidator =
                snapshotValidator;

        this.snapshotPolicy =
                snapshotPolicy;

        this.paginationService =
                paginationService;

        this.clock =
                clock;
    }

    @Override
    @Transactional
    public void procesarProductoSnapshot(
            DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope,
            String payloadJson
    ) {
        try {
            snapshotPolicy
                    .authorizeProcesarSnapshotKafka();

            validarEventoBase(
                    envelope,
                    payloadJson
            );

            ProductoSnapshotPayloadDto payload =
                    envelope.payload();

            validarProductoPayload(
                    payload
            );

            if (
                    productoRepository
                            .existsByEventId(
                                    envelope.eventId()
                            )
            ) {
                log.debug(
                        "Snapshot producto MS3 ignorado por idempotencia. eventId={}",
                        envelope.eventId()
                );

                return;
            }

            ProductoSnapshotMs3 entity =
                    productoRepository
                            .findByIdProductoMs3(
                                    payload.idProducto()
                            )
                            .orElseGet(
                                    ProductoSnapshotMs3::new
                            );

            if (entity.getId() != null) {
                snapshotValidator
                        .validarEventoNoObsoleto(
                                envelope.occurredAt(),
                                entity.getOccurredAt()
                        );

                snapshotValidator
                        .validarVersionEvento(
                                envelope.eventVersionSafe(),
                                entity.getEventVersion()
                        );

                productoMapper
                        .updateFromPayload(
                                entity,
                                payload,
                                envelope,
                                payloadJson
                        );
            } else {
                entity =
                        productoMapper
                                .toEntityFromPayload(
                                        payload,
                                        envelope,
                                        payloadJson
                                );
            }

            productoRepository.save(
                    entity
            );

            procesarSkusAnidados(
                    payload.idProducto(),
                    payload.skus(),
                    Boolean.TRUE.equals(
                            payload.snapshotCompleto()
                    ),
                    envelope,
                    payloadJson
            );

            log.info(
                    "Snapshot producto MS3 procesado. idProductoMs3={}, eventId={}",
                    payload.idProducto(),
                    envelope.eventId()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico procesando snapshot producto MS3. eventId={}, aggregateId={}",
                    envelope == null
                            ? null
                            : envelope.eventId(),
                    envelope == null
                            ? null
                            : envelope.aggregateIdSafe(),
                    ex
            );

            throw internalError(
                    "No se pudo procesar el snapshot de producto MS3.",
                    ex
            );
        }
    }

    @Override
    @Transactional
    public void procesarSkuSnapshot(
            DomainEventEnvelopeDto<SkuSnapshotPayloadDto> envelope,
            String payloadJson
    ) {
        try {
            snapshotPolicy
                    .authorizeProcesarSnapshotKafka();

            validarEventoBase(
                    envelope,
                    payloadJson
            );

            SkuSnapshotPayloadDto payload =
                    envelope.payload();

            validarSkuPayload(
                    payload
            );

            if (
                    skuRepository
                            .existsByEventId(
                                    envelope.eventId()
                            )
            ) {
                log.debug(
                        "Snapshot SKU MS3 ignorado por idempotencia. eventId={}",
                        envelope.eventId()
                );

                return;
            }

            upsertSku(
                    payload,
                    envelope,
                    payloadJson
            );

            log.info(
                    "Snapshot SKU MS3 procesado. idSkuMs3={}, eventId={}",
                    payload.idSku(),
                    envelope.eventId()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico procesando snapshot SKU MS3. eventId={}, aggregateId={}",
                    envelope == null
                            ? null
                            : envelope.eventId(),
                    envelope == null
                            ? null
                            : envelope.aggregateIdSafe(),
                    ex
            );

            throw internalError(
                    "No se pudo procesar el snapshot de SKU MS3.",
                    ex
            );
        }
    }

    @Override
    @Transactional
    public void procesarPrecioSnapshot(
            DomainEventEnvelopeDto<PrecioSnapshotPayloadDto> envelope,
            String payloadJson
    ) {
        try {
            snapshotPolicy
                    .authorizeProcesarSnapshotKafka();

            validarEventoBase(
                    envelope,
                    payloadJson
            );

            PrecioSnapshotPayloadDto payload =
                    envelope.payload();

            validarPrecioPayload(
                    payload
            );

            if (
                    precioRepository
                            .existsByEventId(
                                    envelope.eventId()
                            )
            ) {
                log.debug(
                        "Snapshot precio MS3 ignorado por idempotencia. eventId={}",
                        envelope.eventId()
                );

                return;
            }

            PrecioSnapshotMs3 entity =
                    precioRepository
                            .findByIdPrecioHistorialMs3(
                                    payload.idPrecioHistorial()
                            )
                            .orElseGet(
                                    PrecioSnapshotMs3::new
                            );

            if (entity.getId() != null) {
                snapshotValidator
                        .validarEventoNoObsoleto(
                                envelope.occurredAt(),
                                entity.getOccurredAt()
                        );

                snapshotValidator
                        .validarVersionEvento(
                                envelope.eventVersionSafe(),
                                entity.getEventVersion()
                        );

                precioMapper
                        .updateFromPayload(
                                entity,
                                payload,
                                envelope,
                                payloadJson
                        );
            } else {
                entity =
                        precioMapper
                                .toEntityFromPayload(
                                        payload,
                                        envelope,
                                        payloadJson
                                );
            }

            precioRepository.save(
                    entity
            );

            log.info(
                    "Snapshot precio MS3 procesado. idPrecioHistorialMs3={}, idSkuMs3={}, eventId={}",
                    payload.idPrecioHistorial(),
                    payload.idSku(),
                    envelope.eventId()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico procesando snapshot precio MS3. eventId={}, aggregateId={}",
                    envelope == null
                            ? null
                            : envelope.eventId(),
                    envelope == null
                            ? null
                            : envelope.aggregateIdSafe(),
                    ex
            );

            throw internalError(
                    "No se pudo procesar el snapshot de precio MS3.",
                    ex
            );
        }
    }

    @Override
    @Transactional
    public void procesarPromocionSnapshot(
            DomainEventEnvelopeDto<PromocionSnapshotPayloadDto> envelope,
            String payloadJson
    ) {
        try {
            snapshotPolicy
                    .authorizeProcesarSnapshotKafka();

            validarEventoBase(
                    envelope,
                    payloadJson
            );

            PromocionSnapshotPayloadDto payload =
                    envelope.payload();

            validarPromocionPayload(
                    payload
            );

            if (
                    promocionRepository
                            .existsByEventId(
                                    envelope.eventId()
                            )
            ) {
                log.debug(
                        "Snapshot promoción MS3 ignorado por idempotencia. eventId={}",
                        envelope.eventId()
                );

                return;
            }

            PromocionSnapshotMs3 entity =
                    promocionRepository
                            .findByIdPromocionVersionMs3(
                                    payload.idPromocionVersion()
                            )
                            .orElseGet(
                                    PromocionSnapshotMs3::new
                            );

            if (entity.getId() != null) {
                snapshotValidator
                        .validarEventoNoObsoleto(
                                envelope.occurredAt(),
                                entity.getOccurredAt()
                        );

                snapshotValidator
                        .validarVersionEvento(
                                envelope.eventVersionSafe(),
                                entity.getEventVersion()
                        );

                promocionMapper
                        .updateFromPayload(
                                entity,
                                payload,
                                envelope,
                                payloadJson
                        );
            } else {
                entity =
                        promocionMapper
                                .toEntityFromPayload(
                                        payload,
                                        envelope,
                                        payloadJson
                                );
            }

            promocionRepository.save(
                    entity
            );

            procesarDescuentos(
                    payload.descuentos()
            );

            log.info(
                    "Snapshot promoción MS3 procesado. idPromocionMs3={}, idPromocionVersionMs3={}, eventId={}",
                    payload.idPromocion(),
                    payload.idPromocionVersion(),
                    envelope.eventId()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico procesando snapshot promoción MS3. eventId={}, aggregateId={}",
                    envelope == null
                            ? null
                            : envelope.eventId(),
                    envelope == null
                            ? null
                            : envelope.aggregateIdSafe(),
                    ex
            );

            throw internalError(
                    "No se pudo procesar el snapshot de promoción MS3.",
                    ex
            );
        }
    }

    @Override
    @Transactional
    public void procesarPromocionSkuDescuentoSnapshot(
            DomainEventEnvelopeDto<PromocionSkuDescuentoPayloadDto> envelope,
            String payloadJson
    ) {
        try {
            snapshotPolicy
                    .authorizeProcesarSnapshotKafka();

            validarEventoBase(
                    envelope,
                    payloadJson
            );

            PromocionSkuDescuentoPayloadDto payload =
                    envelope.payload();

            validarDescuentoPayload(
                    payload
            );

            upsertDescuento(
                    payload
            );

            log.info(
                    "Snapshot descuento promoción SKU MS3 procesado. idPromocionSkuDescuentoVersionMs3={}, eventId={}",
                    payload.idPromocionSkuDescuentoVersion(),
                    envelope.eventId()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico procesando snapshot descuento promoción SKU MS3. eventId={}, aggregateId={}",
                    envelope == null
                            ? null
                            : envelope.eventId(),
                    envelope == null
                            ? null
                            : envelope.aggregateIdSafe(),
                    ex
            );

            throw internalError(
                    "No se pudo procesar el snapshot de descuento de promoción MS3.",
                    ex
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoVentaResponseDto
    obtenerProductoVendible(
            Long idProductoMs3
    ) {
        return productoMapper.toResponse(
                resolverProductoVendible(
                        idProductoMs3
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SkuVentaResponseDto
    obtenerSkuVendible(
            Long idSkuMs3
    ) {
        return skuMapper.toResponse(
                resolverSkuVendible(
                        idSkuMs3
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductoVentaResponseDto>
    listarProductosVendibles(
            ProductoVentaFilterDto filter,
            PageRequestDto page
    ) {
        Page<ProductoSnapshotMs3> result =
                productoRepository.findAll(
                        ProductoSnapshotSpecification
                                .build(
                                        filtroProductoVendible(
                                                filter
                                        )
                                ),
                        paginationService
                                .toPageable(
                                        page,
                                        "nombre"
                                )
                );

        return paginationService
                .toPageResponse(
                        result,
                        productoMapper::toResponse
                );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<SkuVentaResponseDto>
    listarSkusVendibles(
            SkuVentaFilterDto filter,
            PageRequestDto page
    ) {
        Page<SkuSnapshotMs3> result =
                skuRepository.findAll(
                        SkuSnapshotSpecification
                                .vendibles(
                                        filter
                                ),
                        paginationService
                                .toPageable(
                                        page,
                                        "codigoSku"
                                )
                );

        return paginationService
                .toPageResponse(
                        result,
                        skuMapper::toResponse
                );
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioSnapshotMs3 resolverPrecioVigente(
            Long idSkuMs3
    ) {
        requirePositive(
                idSkuMs3,
                "El idSkuMs3"
        );

        return precioRepository
                .findFirstByIdSkuMs3AndVigenteTrueAndEstadoTrueOrderByFechaInicioDesc(
                        idSkuMs3
                )
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "No existe precio vigente activo para el SKU MS3: "
                                                + idSkuMs3
                                )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromocionSkuDescuentoSnapshotMs3>
    resolverPromocionAplicable(
            Long idSkuMs3,
            Integer cantidad,
            LocalDateTime fechaOperacion
    ) {
        requirePositive(
                idSkuMs3,
                "El idSkuMs3"
        );

        int cantidadSolicitada =
                cantidad == null
                        ? 1
                        : cantidad;

        if (cantidadSolicitada <= 0) {
            throw new ValidationException(
                    "La cantidad solicitada debe ser mayor a cero."
            );
        }

        LocalDateTime fecha =
                fechaOperacion == null
                        ? LocalDateTime.now(clock)
                        : fechaOperacion;

        return descuentoRepository
                .findByIdSkuMs3AndEstadoTrueOrderByPrioridadAsc(
                        idSkuMs3
                )
                .stream()
                .filter(
                        descuento ->
                                descuento.getLimiteUnidades() == null
                                        || cantidadSolicitada
                                        <= descuento.getLimiteUnidades()
                )
                .filter(
                        descuento ->
                                promocionAplicable(
                                        descuento,
                                        fecha
                                )
                )
                .findFirst();
    }

    private ProductoSnapshotMs3 resolverProductoVendible(
            Long idProductoMs3
    ) {
        requirePositive(
                idProductoMs3,
                "El idProductoMs3"
        );

        ProductoSnapshotMs3 producto =
                productoRepository
                        .findByIdProductoMs3AndEstadoTrue(
                                idProductoMs3
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "No existe producto activo con idProductoMs3: "
                                                        + idProductoMs3
                                        )
                        );

        validarProductoVendible(
                producto
        );

        return producto;
    }

    private SkuSnapshotMs3 resolverSkuVendible(
            Long idSkuMs3
    ) {
        requirePositive(
                idSkuMs3,
                "El idSkuMs3"
        );

        SkuSnapshotMs3 sku =
                skuRepository
                        .findByIdSkuMs3AndEstadoTrue(
                                idSkuMs3
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "No existe SKU activo con idSkuMs3: "
                                                        + idSkuMs3
                                        )
                        );

        ProductoSnapshotMs3 producto =
                productoRepository
                        .findByIdProductoMs3AndEstadoTrue(
                                sku.getIdProductoMs3()
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "No existe producto activo asociado al SKU MS3: "
                                                        + idSkuMs3
                                        )
                        );

        validarProductoVendible(
                producto
        );

        return sku;
    }

    private void procesarSkusAnidados(
            Long idProductoMs3,
            List<SkuSnapshotPayloadDto> skus,
            boolean snapshotCompleto,
            DomainEventEnvelopeDto<ProductoSnapshotPayloadDto> envelope,
            String payloadJson
    ) {
        Set<Long> idsRecibidos =
                new LinkedHashSet<>();

        if (skus != null) {
            for (
                    SkuSnapshotPayloadDto sku
                    : skus
            ) {
                if (sku == null) {
                    continue;
                }

                validarSkuPayload(
                        sku
                );

                if (
                        !idProductoMs3.equals(
                                sku.idProducto()
                        )
                ) {
                    throw new ValidationException(
                            "El SKU "
                                    + sku.idSku()
                                    + " no pertenece al producto "
                                    + idProductoMs3
                                    + "."
                    );
                }

                if (
                        !idsRecibidos.add(
                                sku.idSku()
                        )
                ) {
                    throw new ValidationException(
                            "El snapshot contiene el SKU duplicado: "
                                    + sku.idSku()
                    );
                }

                upsertSku(
                        sku,
                        envelope,
                        payloadJson
                );
            }
        }

        if (!snapshotCompleto) {
            return;
        }

        List<SkuSnapshotMs3> skusLocales =
                skuRepository
                        .findByIdProductoMs3(
                                idProductoMs3
                        );

        LocalDateTime fechaSincronizacion =
                LocalDateTime.now(clock);

        for (
                SkuSnapshotMs3 skuLocal
                : skusLocales
        ) {
            if (
                    idsRecibidos.contains(
                            skuLocal.getIdSkuMs3()
                    )
            ) {
                continue;
            }

            snapshotValidator
                    .validarEventoNoObsoleto(
                            envelope.occurredAt(),
                            skuLocal.getOccurredAt()
                    );

            skuLocal.setEstado(
                    Boolean.FALSE
            );

            skuLocal.setEventId(
                    envelope.eventId()
            );

            skuLocal.setEventType(
                    envelope.eventType()
            );

            skuLocal.setAggregateId(
                    String.valueOf(
                            skuLocal.getIdSkuMs3()
                    )
            );

            skuLocal.setEventVersion(
                    envelope.eventVersionSafe()
            );

            skuLocal.setOccurredAt(
                    envelope.occurredAt()
            );

            skuLocal.setRequestId(
                    envelope.requestId()
            );

            skuLocal.setCorrelationId(
                    envelope.correlationId()
            );

            skuLocal.setPayloadJson(
                    payloadJson
            );

            skuLocal.setFechaSincronizacion(
                    fechaSincronizacion
            );

            skuRepository.save(
                    skuLocal
            );
        }
    }

    private void upsertSku(
            SkuSnapshotPayloadDto payload,
            DomainEventEnvelopeDto<?> envelope,
            String payloadJson
    ) {
        SkuSnapshotMs3 entity =
                skuRepository
                        .findByIdSkuMs3(
                                payload.idSku()
                        )
                        .orElseGet(
                                SkuSnapshotMs3::new
                        );

        if (entity.getId() != null) {
            snapshotValidator
                    .validarEventoNoObsoleto(
                            envelope.occurredAt(),
                            entity.getOccurredAt()
                    );

            snapshotValidator
                    .validarVersionEvento(
                            envelope.eventVersionSafe(),
                            entity.getEventVersion()
                    );

            skuMapper
                    .updateFromPayload(
                            entity,
                            payload,
                            envelope,
                            payloadJson
                    );
        } else {
            entity =
                    skuMapper
                            .toEntityFromPayload(
                                    payload,
                                    envelope,
                                    payloadJson
                            );
        }

        skuRepository.save(
                entity
        );
    }

    private void procesarDescuentos(
            List<PromocionSkuDescuentoPayloadDto> descuentos
    ) {
        if (
                descuentos == null
                        || descuentos.isEmpty()
        ) {
            return;
        }

        descuentos.forEach(
                this::upsertDescuentoValidado
        );
    }

    private void upsertDescuentoValidado(
            PromocionSkuDescuentoPayloadDto payload
    ) {
        validarDescuentoPayload(
                payload
        );

        upsertDescuento(
                payload
        );
    }

    private void upsertDescuento(
            PromocionSkuDescuentoPayloadDto payload
    ) {
        PromocionSkuDescuentoSnapshotMs3 entity =
                descuentoRepository
                        .findByIdPromocionSkuDescuentoVersionMs3(
                                payload
                                        .idPromocionSkuDescuentoVersion()
                        )
                        .orElseGet(
                                PromocionSkuDescuentoSnapshotMs3::new
                        );

        if (entity.getId() != null) {
            promocionMapper
                    .updateDescuentoFromPayload(
                            entity,
                            payload
                    );
        } else {
            entity =
                    promocionMapper
                            .toDescuentoEntityFromPayload(
                                    payload
                            );
        }

        descuentoRepository.save(
                entity
        );
    }

    private boolean promocionAplicable(
            PromocionSkuDescuentoSnapshotMs3 descuento,
            LocalDateTime fecha
    ) {
        if (
                descuento == null
                        || descuento.getIdPromocionMs3() == null
        ) {
            return false;
        }

        return promocionRepository
                .findFirstByIdPromocionMs3AndVigenteTrueAndEstadoTrueOrderByFechaInicioDesc(
                        descuento.getIdPromocionMs3()
                )
                .filter(
                        promocion ->
                                Boolean.TRUE.equals(
                                        promocion
                                                .getVisiblePublico()
                                )
                )
                .filter(
                        promocion ->
                                fechaNoAntes(
                                        fecha,
                                        promocion
                                                .getFechaInicio()
                                )
                )
                .filter(
                        promocion ->
                                fechaNoDespues(
                                        fecha,
                                        promocion
                                                .getFechaFin()
                                )
                )
                .isPresent();
    }

    private boolean fechaNoAntes(
            LocalDateTime fecha,
            LocalDateTime inicio
    ) {
        return inicio == null
                || !fecha.isBefore(inicio);
    }

    private boolean fechaNoDespues(
            LocalDateTime fecha,
            LocalDateTime fin
    ) {
        return fin == null
                || !fecha.isAfter(fin);
    }

    private ProductoVentaFilterDto filtroProductoVendible(
            ProductoVentaFilterDto filter
    ) {
        return new ProductoVentaFilterDto(
                filter == null
                        ? null
                        : filter.search(),

                filter == null
                        ? null
                        : filter.idProductoMs3(),

                filter == null
                        ? null
                        : filter.codigoProducto(),

                filter == null
                        ? null
                        : filter.slug(),

                filter == null
                        ? null
                        : filter.idCategoriaMs3(),

                filter == null
                        ? null
                        : filter.idCategoriaPadreMs3(),

                filter == null
                        ? null
                        : filter.nivelCategoria(),

                Boolean.TRUE,

                Boolean.TRUE,

                filter == null
                        ? null
                        : filter.idMarcaMs3(),

                filter == null
                        ? null
                        : filter.estadoRegistro(),

                filter == null
                        ? null
                        : filter.estadoPublicacion(),

                filter == null
                        ? null
                        : filter.estadoVenta(),

                Boolean.TRUE,

                Boolean.TRUE,

                Boolean.TRUE
        );
    }

    private void validarEventoBase(
            DomainEventEnvelopeDto<?> envelope,
            String payloadJson
    ) {
        snapshotValidator.validarEnvelope(
                envelope
        );

        snapshotValidator.validarPayloadJson(
                payloadJson
        );
    }

    private void validarProductoPayload(
            ProductoSnapshotPayloadDto payload
    ) {
        if (payload == null) {
            throw new ValidationException(
                    "El payload del snapshot de producto MS3 es obligatorio."
            );
        }

        if (
                !Boolean.TRUE.equals(
                        payload.snapshotCompleto()
                )
        ) {
            throw new ValidationException(
                    "El contrato ms3.producto.snapshot.v2 exige snapshotCompleto=true."
            );
        }

        requirePositive(
                payload.idProducto(),
                "El idProducto MS3"
        );

        requirePositive(
                payload.idCategoria(),
                "El idCategoria MS3"
        );

        if (
                !Boolean.TRUE.equals(
                        payload
                                .categoriaPermiteProductos()
                )
        ) {
            throw new ValidationException(
                    "La categoría debe estar habilitada para recibir productos."
            );
        }

        requireText(
                payload.codigoProducto(),
                "El código de producto es obligatorio."
        );

        requireText(
                payload.nombre(),
                "El nombre del producto es obligatorio."
        );
    }

    private void validarSkuPayload(
            SkuSnapshotPayloadDto payload
    ) {
        if (payload == null) {
            throw new ValidationException(
                    "El payload del snapshot de SKU MS3 es obligatorio."
            );
        }

        requirePositive(
                payload.idSku(),
                "El idSku MS3"
        );

        requirePositive(
                payload.idProducto(),
                "El idProducto MS3 del SKU"
        );

        requireText(
                payload.codigoProducto(),
                "El código de producto del SKU es obligatorio."
        );

        requireText(
                payload.codigoSku(),
                "El código SKU es obligatorio."
        );
    }

    private void validarPrecioPayload(
            PrecioSnapshotPayloadDto payload
    ) {
        if (payload == null) {
            throw new ValidationException(
                    "El payload del snapshot de precio MS3 es obligatorio."
            );
        }

        requirePositive(
                payload.idPrecioHistorial(),
                "El idPrecioHistorial MS3"
        );

        requirePositive(
                payload.idSku(),
                "El idSku MS3 del precio"
        );

        requirePositive(
                payload.idProducto(),
                "El idProducto MS3 del precio"
        );

        requireText(
                payload.codigoSku(),
                "El código SKU del precio es obligatorio."
        );

        requireText(
                payload.codigoProducto(),
                "El código de producto del precio es obligatorio."
        );

        requireText(
                payload.nombreProducto(),
                "El nombre de producto del precio es obligatorio."
        );

        requireText(
                payload.moneda(),
                "La moneda del precio es obligatoria."
        );

        if (
                payload.precioVenta() == null
                        || payload.precioVenta()
                        .signum() <= 0
        ) {
            throw new ValidationException(
                    "El precio de venta debe ser mayor a cero."
            );
        }

        if (payload.fechaInicio() == null) {
            throw new ValidationException(
                    "La fecha de inicio del precio es obligatoria."
            );
        }
    }

    private void validarPromocionPayload(
            PromocionSnapshotPayloadDto payload
    ) {
        if (payload == null) {
            throw new ValidationException(
                    "El payload del snapshot de promoción MS3 es obligatorio."
            );
        }

        requirePositive(
                payload.idPromocion(),
                "El idPromocion MS3"
        );

        requirePositive(
                payload.idPromocionVersion(),
                "El idPromocionVersion MS3"
        );

        requireText(
                payload.codigo(),
                "El código de promoción es obligatorio."
        );

        requireText(
                payload.nombre(),
                "El nombre de promoción es obligatorio."
        );

        if (
                payload.fechaInicio() == null
                        || payload.fechaFin() == null
        ) {
            throw new ValidationException(
                    "Las fechas de vigencia de la promoción son obligatorias."
            );
        }

        if (
                payload.fechaFin()
                        .isBefore(
                                payload.fechaInicio()
                        )
        ) {
            throw new ValidationException(
                    "La fecha fin de la promoción no puede ser anterior a la fecha inicio."
            );
        }
    }

    private void validarDescuentoPayload(
            PromocionSkuDescuentoPayloadDto payload
    ) {
        if (payload == null) {
            throw new ValidationException(
                    "El payload del descuento de promoción MS3 es obligatorio."
            );
        }

        requirePositive(
                payload
                        .idPromocionSkuDescuentoVersion(),
                "El idPromocionSkuDescuentoVersion MS3"
        );

        requirePositive(
                payload.idPromocionVersion(),
                "El idPromocionVersion MS3 del descuento"
        );

        requirePositive(
                payload.idPromocion(),
                "El idPromocion MS3 del descuento"
        );

        requirePositive(
                payload.idSku(),
                "El idSku MS3 del descuento"
        );

        requirePositive(
                payload.idProducto(),
                "El idProducto MS3 del descuento"
        );

        requireText(
                payload.codigoSku(),
                "El código SKU del descuento es obligatorio."
        );

        requireText(
                payload.codigoProducto(),
                "El código de producto del descuento es obligatorio."
        );

        requireText(
                payload.nombreProducto(),
                "El nombre de producto del descuento es obligatorio."
        );

        requireText(
                payload.tipoDescuento(),
                "El tipo de descuento es obligatorio."
        );

        if (
                payload.valorDescuento() == null
                        || payload.valorDescuento()
                        .signum() <= 0
        ) {
            throw new ValidationException(
                    "El valor del descuento debe ser mayor a cero."
            );
        }
    }

    private void validarProductoVendible(
            ProductoSnapshotMs3 producto
    ) {
        if (
                producto == null
                        || !Boolean.TRUE.equals(
                        producto.getEstado()
                )
                        || !Boolean.TRUE.equals(
                        producto.getCategoriaEstado()
                )
                        || !Boolean.TRUE.equals(
                        producto
                                .getCategoriaPermiteProductos()
                )
                        || !Boolean.TRUE.equals(
                        producto.getVisiblePublico()
                )
                        || !Boolean.TRUE.equals(
                        producto.getVendible()
                )
        ) {
            throw new NotFoundException(
                    "El producto no se encuentra disponible para venta."
            );
        }
    }

    private void requirePositive(
            Long value,
            String fieldName
    ) {
        if (
                value == null
                        || value <= 0
        ) {
            throw new ValidationException(
                    fieldName
                            + " debe ser un identificador positivo."
            );
        }
    }

    private void requireText(
            String value,
            String message
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new ValidationException(
                    message
            );
        }
    }

    private BusinessException internalError(
            String message,
            RuntimeException ex
    ) {
        return new BusinessException(
                ErrorCodes.INTERNAL_ERROR,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
        );
    }
}
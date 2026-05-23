// ruta: src/main/java/com/upsjb/ms4/service/impl/snapshot/EmpleadoSnapshotServiceImpl.java
package com.upsjb.ms4.service.impl.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.EmpleadoSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.EmpleadoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.EmpleadoSnapshotFilterDto;
import com.upsjb.ms4.dto.snapshot.response.EmpleadoSnapshotResponseDto;
import com.upsjb.ms4.mapper.snapshot.EmpleadoSnapshotMapper;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.snapshot.EmpleadoSnapshotService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.UnauthorizedException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.EmpleadoSnapshotSpecification;
import com.upsjb.ms4.validator.SnapshotValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpleadoSnapshotServiceImpl implements EmpleadoSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(EmpleadoSnapshotServiceImpl.class);

    private static final String RECURSO = "Empleado snapshot MS2";

    private final EmpleadoSnapshotMs2Repository repository;
    private final EmpleadoSnapshotMapper mapper;
    private final SnapshotValidator snapshotValidator;
    private final SnapshotPolicy snapshotPolicy;
    private final PaginationService paginationService;

    public EmpleadoSnapshotServiceImpl(EmpleadoSnapshotMs2Repository repository,
                                       EmpleadoSnapshotMapper mapper,
                                       SnapshotValidator snapshotValidator,
                                       SnapshotPolicy snapshotPolicy,
                                       PaginationService paginationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.snapshotValidator = snapshotValidator;
        this.snapshotPolicy = snapshotPolicy;
        this.paginationService = paginationService;
    }

    @Override
    @Transactional
    public void procesarSnapshotKafka(DomainEventEnvelopeDto<EmpleadoSnapshotPayloadDto> envelope, String payloadJson) {
        try {
            snapshotPolicy.authorizeProcesarSnapshotKafka();
            validarEventoBase(envelope, payloadJson);
            EmpleadoSnapshotPayloadDto payload = envelope.payload();
            validarPayload(payload);

            if (repository.existsByEventId(envelope.eventId())) {
                log.debug("Snapshot empleado MS2 ignorado por idempotencia. eventId={}", envelope.eventId());
                return;
            }

            EmpleadoSnapshotMs2 entity = repository.findByIdEmpleadoMs2(payload.idEmpleado())
                    .orElseGet(EmpleadoSnapshotMs2::new);

            if (entity.getId() != null) {
                snapshotValidator.validarEventoNoObsoleto(envelope.occurredAt(), entity.getOccurredAt());
                snapshotValidator.validarVersionEvento(envelope.eventVersionSafe(), entity.getEventVersion());
                mapper.updateFromPayload(entity, payload, envelope, payloadJson);
            } else {
                entity = mapper.toEntityFromPayload(payload, envelope, payloadJson);
            }

            repository.save(entity);

            log.info("Snapshot empleado MS2 procesado. idEmpleadoMs2={}, idUsuarioMs1={}, eventId={}",
                    payload.idEmpleado(),
                    payload.idUsuarioMs1(),
                    envelope.eventId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico procesando snapshot empleado MS2. eventId={}, aggregateId={}",
                    envelope == null ? null : envelope.eventId(),
                    envelope == null ? null : envelope.aggregateIdSafe(),
                    ex);
            throw internalError("No se pudo procesar el snapshot de empleado MS2.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoSnapshotResponseDto obtenerPorId(Long idSnapshot) {
        return mapper.toResponse(resolverActivoPorId(idSnapshot));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoSnapshotResponseDto obtenerPorUsuarioMs1(Long idUsuarioMs1) {
        return mapper.toResponse(resolverActivoPorUsuarioMs1(idUsuarioMs1));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<EmpleadoSnapshotResponseDto> listar(EmpleadoSnapshotFilterDto filter, PageRequestDto page) {
        Page<EmpleadoSnapshotMs2> result = repository.findAll(
                EmpleadoSnapshotSpecification.build(filter),
                paginationService.toPageable(page, "fechaSincronizacion")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<EmpleadoLookupResponseDto> lookup(LookupFilterDto filter, PageRequestDto page) {
        EmpleadoSnapshotFilterDto snapshotFilter = new EmpleadoSnapshotFilterDto(
                trimToNull(filter == null ? null : filter.search()),
                null,
                null,
                null,
                null,
                null,
                null,
                soloActivos(filter) ? Boolean.TRUE : null,
                soloActivos(filter) ? Boolean.TRUE : null
        );

        Page<EmpleadoSnapshotMs2> result = repository.findAll(
                EmpleadoSnapshotSpecification.build(snapshotFilter),
                paginationService.toPageable(pageForLookup(filter, page), "codigoEmpleado")
        );

        return paginationService.toPageResponse(result, mapper::toLookup);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoSnapshotMs2 resolverActivoPorUsuarioMs1(Long idUsuarioMs1) {
        requirePositive(idUsuarioMs1, "El id de usuario MS1 del empleado");

        EmpleadoSnapshotMs2 entity = repository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException(
                        "No existe empleado activo para el usuario MS1: " + idUsuarioMs1
                ));

        validarActivo(entity);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoSnapshotMs2 resolverActivoPorId(Long idSnapshot) {
        requirePositive(idSnapshot, "El id del snapshot de empleado");

        EmpleadoSnapshotMs2 entity = repository.findById(idSnapshot)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, idSnapshot));

        validarActivo(entity);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public void validarEmpleadoPuedeVender(AuthenticatedUserContext actor) {
        if (actor == null || actor.idUsuarioMs1() == null) {
            throw new UnauthorizedException("No se pudo resolver el empleado autenticado.");
        }

        resolverActivoPorUsuarioMs1(actor.idUsuarioMs1());
    }

    private void validarEventoBase(DomainEventEnvelopeDto<?> envelope, String payloadJson) {
        snapshotValidator.validarEnvelope(envelope);
        snapshotValidator.validarPayloadJson(payloadJson);
    }

    private void validarPayload(EmpleadoSnapshotPayloadDto payload) {
        if (payload == null) {
            throw new ValidationException("El payload del snapshot de empleado MS2 es obligatorio.");
        }

        requirePositive(payload.idEmpleado(), "El idEmpleado MS2");
        requirePositive(payload.idUsuarioMs1(), "El idUsuarioMs1 del empleado");
        requireText(payload.codigoEmpleado(), "El código de empleado es obligatorio.");
    }

    private void validarActivo(EmpleadoSnapshotMs2 entity) {
        if (!esActivo(entity)) {
            throw new NotFoundException("El empleado snapshot MS2 no está activo o no está habilitado en MS2.");
        }
    }

    private boolean esActivo(EmpleadoSnapshotMs2 entity) {
        return entity != null
                && Boolean.TRUE.equals(entity.getEstado())
                && Boolean.TRUE.equals(entity.getEmpleadoActivoMs2());
    }

    private boolean soloActivos(LookupFilterDto filter) {
        return filter == null || filter.soloActivos() == null || Boolean.TRUE.equals(filter.soloActivos());
    }

    private PageRequestDto pageForLookup(LookupFilterDto filter, PageRequestDto page) {
        Integer limit = filter == null ? null : filter.limit();

        if (limit == null) {
            return page;
        }

        return new PageRequestDto(
                page == null ? 0 : page.page(),
                limit,
                page == null ? null : page.sortBy(),
                page == null ? null : page.sortDirection()
        );
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

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BusinessException internalError(String message, RuntimeException ex) {
        return new BusinessException(ErrorCodes.INTERNAL_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}
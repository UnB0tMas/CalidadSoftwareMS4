// ruta: src/main/java/com/upsjb/ms4/service/impl/snapshot/ClienteSnapshotServiceImpl.java
package com.upsjb.ms4.service.impl.snapshot;

import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.dto.kafka.common.DomainEventEnvelopeDto;
import com.upsjb.ms4.dto.kafka.ms2.ClienteSnapshotPayloadDto;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.snapshot.filter.ClienteSnapshotFilterDto;
import com.upsjb.ms4.dto.snapshot.response.ClienteSnapshotResponseDto;
import com.upsjb.ms4.mapper.snapshot.ClienteSnapshotMapper;
import com.upsjb.ms4.policy.SnapshotPolicy;
import com.upsjb.ms4.repository.ClienteSnapshotMs2Repository;
import com.upsjb.ms4.service.contract.snapshot.ClienteSnapshotService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.exception.ValidationException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.ClienteSnapshotSpecification;
import com.upsjb.ms4.validator.SnapshotValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteSnapshotServiceImpl implements ClienteSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(ClienteSnapshotServiceImpl.class);

    private static final String RECURSO = "Cliente snapshot MS2";

    private final ClienteSnapshotMs2Repository repository;
    private final ClienteSnapshotMapper mapper;
    private final SnapshotValidator snapshotValidator;
    private final SnapshotPolicy snapshotPolicy;
    private final PaginationService paginationService;

    public ClienteSnapshotServiceImpl(ClienteSnapshotMs2Repository repository,
                                      ClienteSnapshotMapper mapper,
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
    public void procesarSnapshotKafka(DomainEventEnvelopeDto<ClienteSnapshotPayloadDto> envelope, String payloadJson) {
        try {
            snapshotPolicy.authorizeProcesarSnapshotKafka();
            validarEventoBase(envelope, payloadJson);
            ClienteSnapshotPayloadDto payload = envelope.payload();
            validarPayload(payload);

            if (repository.existsByEventId(envelope.eventId())) {
                log.debug("Snapshot cliente MS2 ignorado por idempotencia. eventId={}", envelope.eventId());
                return;
            }

            ClienteSnapshotMs2 entity = repository.findByIdClienteMs2(payload.idCliente())
                    .orElseGet(ClienteSnapshotMs2::new);

            if (entity.getId() != null) {
                snapshotValidator.validarEventoNoObsoleto(envelope.occurredAt(), entity.getOccurredAt());
                snapshotValidator.validarVersionEvento(envelope.eventVersionSafe(), entity.getEventVersion());
                mapper.updateFromPayload(entity, payload, envelope, payloadJson);
            } else {
                entity = mapper.toEntityFromPayload(payload, envelope, payloadJson);
            }

            repository.save(entity);

            log.info("Snapshot cliente MS2 procesado. idClienteMs2={}, idUsuarioMs1={}, eventId={}",
                    payload.idCliente(),
                    payload.idUsuarioMs1(),
                    envelope.eventId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error técnico procesando snapshot cliente MS2. eventId={}, aggregateId={}",
                    envelope == null ? null : envelope.eventId(),
                    envelope == null ? null : envelope.aggregateIdSafe(),
                    ex);
            throw internalError("No se pudo procesar el snapshot de cliente MS2.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSnapshotResponseDto obtenerPorId(Long idSnapshot) {
        return mapper.toResponse(resolverActivoPorId(idSnapshot));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSnapshotResponseDto obtenerPorUsuarioMs1(Long idUsuarioMs1) {
        return mapper.toResponse(resolverActivoPorUsuarioMs1(idUsuarioMs1));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ClienteSnapshotResponseDto> listar(ClienteSnapshotFilterDto filter, PageRequestDto page) {
        Page<ClienteSnapshotMs2> result = repository.findAll(
                ClienteSnapshotSpecification.build(filter),
                paginationService.toPageable(page, "fechaSincronizacion")
        );

        return paginationService.toPageResponse(result, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ClienteLookupResponseDto> lookup(LookupFilterDto filter, PageRequestDto page) {
        ClienteSnapshotFilterDto snapshotFilter = new ClienteSnapshotFilterDto(
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

        Page<ClienteSnapshotMs2> result = repository.findAll(
                ClienteSnapshotSpecification.build(snapshotFilter),
                paginationService.toPageable(pageForLookup(filter, page), "nombreCompleto")
        );

        return paginationService.toPageResponse(result, mapper::toLookup);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSnapshotMs2 resolverActivoPorId(Long idSnapshot) {
        requirePositive(idSnapshot, "El id del snapshot de cliente");

        ClienteSnapshotMs2 entity = repository.findById(idSnapshot)
                .orElseThrow(() -> NotFoundException.byId(RECURSO, idSnapshot));

        validarActivo(entity);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSnapshotMs2 resolverActivoPorUsuarioMs1(Long idUsuarioMs1) {
        requirePositive(idUsuarioMs1, "El id de usuario MS1 del cliente");

        ClienteSnapshotMs2 entity = repository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .orElseThrow(() -> new NotFoundException(
                        "No existe cliente activo para el usuario MS1: " + idUsuarioMs1
                ));

        validarActivo(entity);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeClienteActivoPorUsuarioMs1(Long idUsuarioMs1) {
        if (idUsuarioMs1 == null || idUsuarioMs1 <= 0) {
            return false;
        }

        return repository.findByIdUsuarioMs1AndEstadoTrue(idUsuarioMs1)
                .filter(this::esActivo)
                .isPresent();
    }

    private void validarEventoBase(DomainEventEnvelopeDto<?> envelope, String payloadJson) {
        snapshotValidator.validarEnvelope(envelope);
        snapshotValidator.validarPayloadJson(payloadJson);
    }

    private void validarPayload(ClienteSnapshotPayloadDto payload) {
        if (payload == null) {
            throw new ValidationException("El payload del snapshot de cliente MS2 es obligatorio.");
        }

        requirePositive(payload.idCliente(), "El idCliente MS2");
        requirePositive(payload.idUsuarioMs1(), "El idUsuarioMs1 del cliente");
        requireText(payload.tipoCliente(), "El tipo de cliente es obligatorio.");
    }

    private void validarActivo(ClienteSnapshotMs2 entity) {
        if (!esActivo(entity)) {
            throw new NotFoundException("El cliente snapshot MS2 no está activo o no está habilitado en MS2.");
        }
    }

    private boolean esActivo(ClienteSnapshotMs2 entity) {
        return entity != null
                && Boolean.TRUE.equals(entity.getEstado())
                && Boolean.TRUE.equals(entity.getClienteActivoMs2());
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
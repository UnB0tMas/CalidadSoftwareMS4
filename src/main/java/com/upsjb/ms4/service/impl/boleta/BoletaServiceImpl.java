// ruta: src/main/java/com/upsjb/ms4/service/impl/boleta/BoletaServiceImpl.java
package com.upsjb.ms4.service.impl.boleta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.boleta.BoletaDetalle;
import com.upsjb.ms4.domain.entity.config.BoletaPlantillaVersion;
import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import com.upsjb.ms4.domain.entity.config.ConfiguracionTributariaVersion;
import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.EstadoBoleta;
import com.upsjb.ms4.domain.enums.NombreImpuesto;
import com.upsjb.ms4.domain.enums.TipoCorreo;
import com.upsjb.ms4.dto.boleta.filter.BoletaFilterDto;
import com.upsjb.ms4.dto.boleta.response.BoletaDetailResponseDto;
import com.upsjb.ms4.dto.boleta.response.BoletaDetalleResponseDto;
import com.upsjb.ms4.dto.boleta.response.BoletaResponseDto;
import com.upsjb.ms4.dto.mail.response.CorreoOutboxResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.mapper.boleta.BoletaDetalleMapper;
import com.upsjb.ms4.mapper.boleta.BoletaMapper;
import com.upsjb.ms4.policy.BoletaPolicy;
import com.upsjb.ms4.repository.BoletaDetalleRepository;
import com.upsjb.ms4.repository.BoletaPlantillaVersionRepository;
import com.upsjb.ms4.repository.BoletaRepository;
import com.upsjb.ms4.repository.ConfiguracionEmpresaVersionRepository;
import com.upsjb.ms4.repository.ConfiguracionTributariaVersionRepository;
import com.upsjb.ms4.repository.SerieBoletaRepository;
import com.upsjb.ms4.repository.VentaDetalleRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.auditoria.AuditoriaFuncionalService;
import com.upsjb.ms4.service.contract.boleta.BoletaService;
import com.upsjb.ms4.service.contract.mail.CorreoOutboxService;
import com.upsjb.ms4.shared.constants.ErrorCodes;
import com.upsjb.ms4.shared.exception.BusinessException;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.BoletaSpecification;
import com.upsjb.ms4.util.HashUtil;
import com.upsjb.ms4.util.JsonUtil;
import com.upsjb.ms4.validator.BoletaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoletaServiceImpl implements BoletaService {

    private static final Logger log = LoggerFactory.getLogger(BoletaServiceImpl.class);

    private static final String RECURSO_BOLETA = "Boleta";
    private static final String RECURSO_VENTA = "Venta";
    private static final String ENTIDAD_BOLETA = "BOLETA";
    private static final String ENTIDAD_ORIGEN_BOLETA = "BOLETA";
    private static final String ACCION_EMITIR_BOLETA = "EMITIR_BOLETA";
    private static final String ACCION_PROGRAMAR_CORREO_BOLETA = "PROGRAMAR_CORREO_BOLETA";
    private static final String PAYLOAD_INICIAL = "{}";

    private final BoletaRepository boletaRepository;
    private final BoletaDetalleRepository boletaDetalleRepository;
    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final SerieBoletaRepository serieBoletaRepository;
    private final ConfiguracionEmpresaVersionRepository configuracionEmpresaRepository;
    private final ConfiguracionTributariaVersionRepository configuracionTributariaRepository;
    private final BoletaPlantillaVersionRepository boletaPlantillaRepository;
    private final BoletaMapper boletaMapper;
    private final BoletaDetalleMapper boletaDetalleMapper;
    private final BoletaPolicy boletaPolicy;
    private final BoletaValidator boletaValidator;
    private final PaginationService paginationService;
    private final CorreoOutboxService correoOutboxService;
    private final AuditoriaFuncionalService auditoriaFuncionalService;
    private final Clock clock;

    public BoletaServiceImpl(BoletaRepository boletaRepository,
                             BoletaDetalleRepository boletaDetalleRepository,
                             VentaRepository ventaRepository,
                             VentaDetalleRepository ventaDetalleRepository,
                             SerieBoletaRepository serieBoletaRepository,
                             ConfiguracionEmpresaVersionRepository configuracionEmpresaRepository,
                             ConfiguracionTributariaVersionRepository configuracionTributariaRepository,
                             BoletaPlantillaVersionRepository boletaPlantillaRepository,
                             BoletaMapper boletaMapper,
                             BoletaDetalleMapper boletaDetalleMapper,
                             BoletaPolicy boletaPolicy,
                             BoletaValidator boletaValidator,
                             PaginationService paginationService,
                             CorreoOutboxService correoOutboxService,
                             AuditoriaFuncionalService auditoriaFuncionalService,
                             Clock clock) {
        this.boletaRepository = boletaRepository;
        this.boletaDetalleRepository = boletaDetalleRepository;
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.serieBoletaRepository = serieBoletaRepository;
        this.configuracionEmpresaRepository = configuracionEmpresaRepository;
        this.configuracionTributariaRepository = configuracionTributariaRepository;
        this.boletaPlantillaRepository = boletaPlantillaRepository;
        this.boletaMapper = boletaMapper;
        this.boletaDetalleMapper = boletaDetalleMapper;
        this.boletaPolicy = boletaPolicy;
        this.boletaValidator = boletaValidator;
        this.paginationService = paginationService;
        this.correoOutboxService = correoOutboxService;
        this.auditoriaFuncionalService = auditoriaFuncionalService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public BoletaDetailResponseDto emitirBoletaPorVentaConfirmada(Long idVenta, AuthenticatedUserContext actor) {
        try {
            if (idVenta == null || idVenta <= 0) {
                throw new BusinessException(ErrorCodes.INVALID_PARAMETER, "El id de venta debe ser positivo.");
            }

            Venta venta = ventaRepository.findByIdAndEstadoTrue(idVenta)
                    .orElseThrow(() -> NotFoundException.byId(RECURSO_VENTA, idVenta));

            boletaValidator.validarVentaConfirmada(venta);

            Boleta existenteActiva = boletaRepository.findByIdVentaAndEstadoTrue(idVenta).orElse(null);
            if (existenteActiva != null) {
                return construirDetalleResponse(existenteActiva);
            }

            Boleta existente = boletaRepository.findByIdVenta(idVenta).orElse(null);
            boletaValidator.validarBoletaNoDuplicadaPorVenta(existente);

            List<VentaDetalle> detallesVenta = ventaDetalleRepository.findByIdVentaAndEstadoTrueOrderByIdAsc(idVenta);
            boletaValidator.validarDetallesVentaParaBoleta(detallesVenta);

            ConfiguracionEmpresaVersion empresa = resolverConfiguracionEmpresaVigente();
            ConfiguracionTributariaVersion tributaria = resolverConfiguracionTributariaUsada(venta);
            BoletaPlantillaVersion plantilla = resolverPlantillaVigente();
            SerieBoleta serie = resolverSerieDisponibleConLock();

            Long numero = serie.getNumeroActual() + 1;
            String codigoBoleta = construirCodigoBoleta(serie.getSerie(), numero);

            if (boletaRepository.existsByCodigoBoletaIgnoreCase(codigoBoleta)) {
                throw new BusinessException(
                        ErrorCodes.CONFLICT,
                        "Ya existe una boleta con el código generado: " + codigoBoleta,
                        HttpStatus.CONFLICT
                );
            }

            serie.setNumeroActual(numero);
            serieBoletaRepository.save(serie);

            Boleta boleta = construirBoleta(venta, empresa, tributaria, plantilla, serie, numero, codigoBoleta);
            boleta = boletaRepository.save(boleta);

            final Long idBoletaGenerada = boleta.getId();

            List<BoletaDetalle> detalles = detallesVenta.stream()
                    .map(detalleVenta -> construirDetalleBoleta(idBoletaGenerada, detalleVenta))
                    .toList();

            List<BoletaDetalle> detallesGuardados = boletaDetalleRepository.saveAll(detalles);

            String payloadJson = JsonUtil.toCanonicalJson(construirPayloadBoleta(boleta, detallesGuardados, venta));
            boleta.setPayloadJson(payloadJson);
            boleta.setHashPayload(HashUtil.sha256(payloadJson));
            boleta = boletaRepository.save(boleta);

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_BOLETA,
                    boleta.getId(),
                    ACCION_EMITIR_BOLETA,
                    actor,
                    Map.of(
                            "idVenta", venta.getId(),
                            "codigoVenta", venta.getCodigoVenta(),
                            "codigoBoleta", boleta.getCodigoBoleta()
                    )
            );

            return construirDetalleResponse(boleta, detallesGuardados);
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_BOLETA,
                    idVenta,
                    ACCION_EMITIR_BOLETA,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico emitiendo boleta por venta confirmada. idVenta={}, actorIdUsuarioMs1={}, requestActor={}",
                    idVenta,
                    actor == null ? null : actor.idUsuarioMs1(),
                    actor == null ? null : actor.actorLabel(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_BOLETA,
                    idVenta,
                    ACCION_EMITIR_BOLETA,
                    actor,
                    ex
            );
            throw internalError("No se pudo emitir la boleta de la venta.", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaDetailResponseDto obtenerDetalleAdmin(Long idBoleta, AuthenticatedUserContext actor) {
        boletaPolicy.authorizeVerBoletaAdmin(actor);
        Boleta boleta = resolverBoletaActiva(idBoleta);
        return construirDetalleResponse(boleta);
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaDetailResponseDto obtenerDetalleEmpleado(Long idBoleta, AuthenticatedUserContext actor) {
        boletaPolicy.authorizeListarBoletasEmpleado(actor);
        Boleta boleta = resolverBoletaActiva(idBoleta);
        boletaPolicy.authorizeVerBoleta(actor, boleta);
        return construirDetalleResponse(boleta);
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaDetailResponseDto obtenerDetalleCliente(Long idBoleta, AuthenticatedUserContext actor) {
        boletaPolicy.authorizeListarMisBoletasCliente(actor);
        Boleta boleta = resolverBoletaActiva(idBoleta);
        boletaPolicy.authorizeVerBoleta(actor, boleta);
        return construirDetalleResponse(boleta);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BoletaResponseDto> listarAdmin(BoletaFilterDto filter,
                                                          PageRequestDto page,
                                                          AuthenticatedUserContext actor) {
        boletaPolicy.authorizeListarBoletasAdmin(actor);
        boletaValidator.validarFiltro(filter);

        Page<Boleta> result = boletaRepository.findAll(
                BoletaSpecification.build(filter),
                paginationService.toPageable(page, "fechaEmision")
        );

        return paginationService.toPageResponse(result, boletaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BoletaResponseDto> listarEmpleado(BoletaFilterDto filter,
                                                             PageRequestDto page,
                                                             AuthenticatedUserContext actor) {
        boletaPolicy.authorizeListarBoletasEmpleado(actor);
        boletaValidator.validarFiltro(filter);

        Specification<Boleta> spec = BoletaSpecification.build(filter);
        if (!actor.isAdmin()) {
            spec = spec.and(BoletaSpecification.delEmpleadoUsuario(actor.idUsuarioMs1()));
        }

        Page<Boleta> result = boletaRepository.findAll(
                spec,
                paginationService.toPageable(page, "fechaEmision")
        );

        return paginationService.toPageResponse(result, boletaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BoletaResponseDto> listarCliente(BoletaFilterDto filter,
                                                            PageRequestDto page,
                                                            AuthenticatedUserContext actor) {
        boletaPolicy.authorizeListarMisBoletasCliente(actor);
        boletaValidator.validarFiltro(filter);

        Specification<Boleta> spec = BoletaSpecification.build(filter)
                .and(BoletaSpecification.delClienteUsuario(actor.idUsuarioMs1()));

        Page<Boleta> result = boletaRepository.findAll(
                spec,
                paginationService.toPageable(page, "fechaEmision")
        );

        return paginationService.toPageResponse(result, boletaMapper::toResponse);
    }

    @Override
    @Transactional
    public CorreoOutboxResponseDto programarCorreoBoleta(Long idBoleta,
                                                         TipoCorreo tipoCorreo,
                                                         AuthenticatedUserContext actor) {
        try {
            boletaValidator.validarTipoCorreoBoleta(tipoCorreo);

            Boleta boleta = resolverBoletaActiva(idBoleta);
            boletaPolicy.authorizeReenviarCorreo(actor, boleta);
            boletaValidator.validarCorreoCliente(boleta.getCorreoCliente());

            CorreoOutboxResponseDto correoProgramado = correoOutboxService.programarCorreo(
                    tipoCorreo,
                    ENTIDAD_ORIGEN_BOLETA,
                    boleta.getId(),
                    boleta.getId(),
                    boleta.getCorreoCliente(),
                    boleta.getNombreCliente(),
                    construirAsuntoBoleta(boleta),
                    actor
            );

            auditoriaFuncionalService.registrarExito(
                    ENTIDAD_BOLETA,
                    boleta.getId(),
                    ACCION_PROGRAMAR_CORREO_BOLETA,
                    actor,
                    Map.of(
                            "codigoBoleta", boleta.getCodigoBoleta(),
                            "tipoCorreo", tipoCorreo.getCode(),
                            "destinatario", boleta.getCorreoCliente()
                    )
            );

            return correoProgramado;
        } catch (BusinessException ex) {
            auditoriaFuncionalService.registrarErrorUsuario(
                    ENTIDAD_BOLETA,
                    idBoleta,
                    ACCION_PROGRAMAR_CORREO_BOLETA,
                    actor,
                    ex.getCode(),
                    ex.getMessage()
            );
            throw ex;
        } catch (RuntimeException ex) {
            log.error(
                    "Error técnico programando correo de boleta. idBoleta={}, tipoCorreo={}, actorIdUsuarioMs1={}",
                    idBoleta,
                    tipoCorreo,
                    actor == null ? null : actor.idUsuarioMs1(),
                    ex
            );
            auditoriaFuncionalService.registrarErrorTecnico(
                    ENTIDAD_BOLETA,
                    idBoleta,
                    ACCION_PROGRAMAR_CORREO_BOLETA,
                    actor,
                    ex
            );
            throw internalError("No se pudo programar el correo de la boleta.", ex);
        }
    }

    @Override
    @Transactional
    public void marcarBoletaEnviadaPorCorreo(Long idBoleta) {
        Boleta boleta = resolverBoletaActiva(idBoleta);
        int cantidadActual = boleta.getCantidadEnviosCorreo() == null ? 0 : boleta.getCantidadEnviosCorreo();

        boleta.setEnviadoPorCorreo(true);
        boleta.setFechaUltimoEnvioCorreo(LocalDateTime.now(clock));
        boleta.setCantidadEnviosCorreo(cantidadActual + 1);

        if (boleta.getEstadoBoleta() == EstadoBoleta.ERROR_ENVIO_CORREO) {
            boleta.setEstadoBoleta(EstadoBoleta.EMITIDA);
        }

        boletaRepository.save(boleta);
    }

    @Override
    @Transactional(readOnly = true)
    public Boleta resolverBoletaParaRender(Long idBoleta) {
        return resolverBoletaActiva(idBoleta);
    }

    private Boleta resolverBoletaActiva(Long idBoleta) {
        boletaValidator.validarIdBoleta(idBoleta);

        return boletaRepository.findByIdAndEstadoTrue(idBoleta)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_BOLETA, idBoleta));
    }

    private ConfiguracionEmpresaVersion resolverConfiguracionEmpresaVigente() {
        return configuracionEmpresaRepository.findFirstByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc()
                .orElseThrow(() -> new NotFoundException("No existe configuración empresarial vigente para emitir boleta."));
    }

    private ConfiguracionTributariaVersion resolverConfiguracionTributariaUsada(Venta venta) {
        if (venta.getIdConfiguracionTributariaVersion() != null) {
            return configuracionTributariaRepository.findById(venta.getIdConfiguracionTributariaVersion())
                    .filter(ConfiguracionTributariaVersion::isActivo)
                    .orElseThrow(() -> new NotFoundException(
                            "No se encontró la configuración tributaria usada por la venta."
                    ));
        }

        return configuracionTributariaRepository
                .findFirstByNombreImpuestoAndVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc(NombreImpuesto.IGV)
                .orElseThrow(() -> new NotFoundException("No existe configuración tributaria IGV vigente."));
    }

    private BoletaPlantillaVersion resolverPlantillaVigente() {
        return boletaPlantillaRepository.findFirstByVigenteTrueAndEstadoTrueOrderByFechaInicioVigenciaDesc()
                .orElseThrow(() -> new NotFoundException("No existe plantilla de boleta vigente."));
    }

    private SerieBoleta resolverSerieDisponibleConLock() {
        SerieBoleta serieBase = serieBoletaRepository.findFirstByEstadoTrueOrderBySerieAsc()
                .orElseThrow(() -> new NotFoundException("No existe una serie de boleta activa."));

        SerieBoleta serie = serieBoletaRepository.findByIdForUpdate(serieBase.getId())
                .orElseThrow(() -> new NotFoundException("No se pudo bloquear la serie de boleta para numeración."));

        boletaValidator.validarSerieDisponible(serie);
        return serie;
    }

    private Boleta construirBoleta(Venta venta,
                                   ConfiguracionEmpresaVersion empresa,
                                   ConfiguracionTributariaVersion tributaria,
                                   BoletaPlantillaVersion plantilla,
                                   SerieBoleta serie,
                                   Long numero,
                                   String codigoBoleta) {
        ClienteSnapshotMs2 cliente = venta.getClienteSnapshot();

        return Boleta.builder()
                .idVenta(venta.getId())
                .idSerieBoleta(serie.getId())
                .idConfiguracionEmpresaVersion(empresa.getId())
                .idConfiguracionTributariaVersion(tributaria.getId())
                .idBoletaPlantillaVersion(plantilla.getId())
                .serie(serie.getSerie())
                .numero(numero)
                .codigoBoleta(codigoBoleta)
                .fechaEmision(LocalDateTime.now(clock))
                .moneda(venta.getMoneda())
                .rucEmisor(empresa.getRuc())
                .razonSocialEmisor(empresa.getRazonSocial())
                .nombreComercialEmisor(empresa.getNombreComercial())
                .direccionFiscalEmisor(empresa.getDireccionFiscal())
                .telefonoEmisor(empresa.getTelefono())
                .correoEmisor(empresa.getCorreo())
                .logoUrlEmisor(empresa.getLogoUrl())
                .tipoDocumentoCliente(resolverTipoDocumentoCliente(cliente))
                .numeroDocumentoCliente(resolverNumeroDocumentoCliente(cliente))
                .nombreCliente(resolverNombreCliente(cliente))
                .correoCliente(cliente == null ? null : cliente.getCorreoPrincipal())
                .telefonoCliente(cliente == null ? null : cliente.getTelefonoPrincipal())
                .direccionCliente(cliente == null ? null : cliente.getDireccionPrincipal())
                .subtotal(venta.getSubtotal())
                .descuentoTotal(venta.getDescuentoTotal())
                .opGravada(venta.getOpGravada())
                .opExonerada(venta.getOpExonerada())
                .opInafecta(venta.getOpInafecta())
                .igvPorcentaje(venta.getIgvPorcentaje())
                .igvTotal(venta.getIgvTotal())
                .total(venta.getTotal())
                .estadoBoleta(EstadoBoleta.EMITIDA)
                .payloadJson(PAYLOAD_INICIAL)
                .hashPayload(HashUtil.sha256(PAYLOAD_INICIAL))
                .versionPlantilla(plantilla.getCodigoVersion())
                .enviadoPorCorreo(false)
                .cantidadEnviosCorreo(0)
                .estado(true)
                .build();
    }

    private BoletaDetalle construirDetalleBoleta(Long idBoleta, VentaDetalle detalleVenta) {
        return BoletaDetalle.builder()
                .idBoleta(idBoleta)
                .idVentaDetalle(detalleVenta.getId())
                .idProductoMs3(detalleVenta.getIdProductoMs3())
                .idSkuMs3(detalleVenta.getIdSkuMs3())
                .codigoProducto(detalleVenta.getCodigoProducto())
                .codigoSku(detalleVenta.getCodigoSku())
                .nombreProducto(detalleVenta.getNombreProducto())
                .descripcionSku(detalleVenta.getDescripcionSku())
                .cantidad(detalleVenta.getCantidad())
                .precioUnitarioBase(detalleVenta.getPrecioUnitarioBase())
                .precioUnitarioFinal(detalleVenta.getPrecioUnitarioFinal())
                .subtotal(detalleVenta.getSubtotal())
                .tipoDescuento(detalleVenta.getTipoDescuento())
                .valorDescuento(detalleVenta.getValorDescuento())
                .montoDescuento(detalleVenta.getMontoDescuento())
                .idPromocionMs3(detalleVenta.getIdPromocionMs3())
                .idPromocionVersionMs3(detalleVenta.getIdPromocionVersionMs3())
                .codigoPromocion(detalleVenta.getCodigoPromocion())
                .igvPorcentaje(detalleVenta.getIgvPorcentaje())
                .igvMonto(detalleVenta.getIgvMonto())
                .totalLinea(detalleVenta.getTotalLinea())
                .payloadProductoSnapshotJson(detalleVenta.getPayloadProductoSnapshotJson())
                .payloadSkuSnapshotJson(detalleVenta.getPayloadSkuSnapshotJson())
                .payloadPrecioSnapshotJson(detalleVenta.getPayloadPrecioSnapshotJson())
                .payloadPromocionSnapshotJson(detalleVenta.getPayloadPromocionSnapshotJson())
                .estado(true)
                .build();
    }

    private Map<String, Object> construirPayloadBoleta(Boleta boleta,
                                                       List<BoletaDetalle> detalles,
                                                       Venta venta) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("idBoleta", boleta.getId());
        payload.put("codigoBoleta", boleta.getCodigoBoleta());
        payload.put("serie", boleta.getSerie());
        payload.put("numero", boleta.getNumero());
        payload.put("fechaEmision", boleta.getFechaEmision());
        payload.put("versionPlantilla", boleta.getVersionPlantilla());

        payload.put("venta", Map.of(
                "idVenta", venta.getId(),
                "codigoVenta", venta.getCodigoVenta(),
                "canalVenta", venta.getCanalVenta(),
                "estadoVenta", venta.getEstadoVenta()
        ));

        payload.put("emisor", Map.of(
                "ruc", boleta.getRucEmisor(),
                "razonSocial", boleta.getRazonSocialEmisor(),
                "nombreComercial", safe(boleta.getNombreComercialEmisor()),
                "direccionFiscal", boleta.getDireccionFiscalEmisor(),
                "telefono", safe(boleta.getTelefonoEmisor()),
                "correo", safe(boleta.getCorreoEmisor()),
                "logoUrl", safe(boleta.getLogoUrlEmisor())
        ));

        payload.put("cliente", Map.of(
                "tipoDocumento", safe(boleta.getTipoDocumentoCliente()),
                "numeroDocumento", safe(boleta.getNumeroDocumentoCliente()),
                "nombre", boleta.getNombreCliente(),
                "correo", safe(boleta.getCorreoCliente()),
                "telefono", safe(boleta.getTelefonoCliente()),
                "direccion", safe(boleta.getDireccionCliente())
        ));

        payload.put("totales", Map.of(
                "moneda", boleta.getMoneda(),
                "subtotal", boleta.getSubtotal(),
                "descuentoTotal", boleta.getDescuentoTotal(),
                "opGravada", boleta.getOpGravada(),
                "opExonerada", boleta.getOpExonerada(),
                "opInafecta", boleta.getOpInafecta(),
                "igvPorcentaje", boleta.getIgvPorcentaje(),
                "igvTotal", boleta.getIgvTotal(),
                "total", boleta.getTotal()
        ));

        payload.put("detalles", detalles.stream().map(this::detallePayload).toList());

        return payload;
    }

    private Map<String, Object> detallePayload(BoletaDetalle detalle) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("idVentaDetalle", detalle.getIdVentaDetalle());
        payload.put("idProductoMs3", detalle.getIdProductoMs3());
        payload.put("idSkuMs3", detalle.getIdSkuMs3());
        payload.put("codigoProducto", detalle.getCodigoProducto());
        payload.put("codigoSku", detalle.getCodigoSku());
        payload.put("nombreProducto", detalle.getNombreProducto());
        payload.put("descripcionSku", safe(detalle.getDescripcionSku()));
        payload.put("cantidad", detalle.getCantidad());
        payload.put("precioUnitarioBase", detalle.getPrecioUnitarioBase());
        payload.put("precioUnitarioFinal", detalle.getPrecioUnitarioFinal());
        payload.put("subtotal", detalle.getSubtotal());
        payload.put("tipoDescuento", detalle.getTipoDescuento());
        payload.put("valorDescuento", detalle.getValorDescuento());
        payload.put("montoDescuento", detalle.getMontoDescuento());
        payload.put("idPromocionMs3", detalle.getIdPromocionMs3());
        payload.put("idPromocionVersionMs3", detalle.getIdPromocionVersionMs3());
        payload.put("codigoPromocion", safe(detalle.getCodigoPromocion()));
        payload.put("igvPorcentaje", detalle.getIgvPorcentaje());
        payload.put("igvMonto", detalle.getIgvMonto());
        payload.put("totalLinea", detalle.getTotalLinea());
        return payload;
    }

    private BoletaDetailResponseDto construirDetalleResponse(Boleta boleta) {
        List<BoletaDetalle> detalles = boletaDetalleRepository.findByIdBoletaAndEstadoTrueOrderByIdAsc(boleta.getId());
        return construirDetalleResponse(boleta, detalles);
    }

    private BoletaDetailResponseDto construirDetalleResponse(Boleta boleta, List<BoletaDetalle> detalles) {
        List<BoletaDetalleResponseDto> detalleDtos = detalles.stream()
                .map(boletaDetalleMapper::toResponse)
                .toList();

        return boletaMapper.toDetailResponse(boleta, detalleDtos);
    }

    private String resolverTipoDocumentoCliente(ClienteSnapshotMs2 cliente) {
        if (cliente == null) {
            return null;
        }

        if (!isBlank(cliente.getRuc())) {
            return "RUC";
        }

        return cliente.getTipoDocumentoPersona();
    }

    private String resolverNumeroDocumentoCliente(ClienteSnapshotMs2 cliente) {
        if (cliente == null) {
            return null;
        }

        if (!isBlank(cliente.getRuc())) {
            return cliente.getRuc();
        }

        return cliente.getNumeroDocumentoPersona();
    }

    private String resolverNombreCliente(ClienteSnapshotMs2 cliente) {
        if (cliente == null) {
            return "CLIENTE NO IDENTIFICADO";
        }

        if (!isBlank(cliente.getRazonSocial())) {
            return cliente.getRazonSocial();
        }

        if (!isBlank(cliente.getNombreComercial())) {
            return cliente.getNombreComercial();
        }

        if (!isBlank(cliente.getNombreCompleto())) {
            return cliente.getNombreCompleto();
        }

        String nombre = String.join(
                " ",
                safe(cliente.getNombres()),
                safe(cliente.getApePaterno()),
                safe(cliente.getApeMaterno())
        ).trim();

        return nombre.isBlank() ? "CLIENTE NO IDENTIFICADO" : nombre;
    }

    private String construirCodigoBoleta(String serie, Long numero) {
        return serie + "-" + String.format("%08d", numero);
    }

    private String construirAsuntoBoleta(Boleta boleta) {
        return "Boleta " + boleta.getCodigoBoleta();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
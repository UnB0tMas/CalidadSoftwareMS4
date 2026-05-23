// ruta: src/main/java/com/upsjb/ms4/service/impl/venta/VentaConsultaServiceImpl.java
package com.upsjb.ms4.service.impl.venta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.dto.boleta.response.BoletaResponseDto;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.dto.venta.filter.VentaFilterDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetalleResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResponseDto;
import com.upsjb.ms4.mapper.boleta.BoletaMapper;
import com.upsjb.ms4.mapper.pago.PagoMapper;
import com.upsjb.ms4.mapper.venta.VentaDetalleMapper;
import com.upsjb.ms4.mapper.venta.VentaMapper;
import com.upsjb.ms4.policy.VentaPolicy;
import com.upsjb.ms4.repository.BoletaRepository;
import com.upsjb.ms4.repository.PagoRepository;
import com.upsjb.ms4.repository.VentaDetalleRepository;
import com.upsjb.ms4.repository.VentaRepository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.venta.VentaConsultaService;
import com.upsjb.ms4.shared.exception.NotFoundException;
import com.upsjb.ms4.shared.pagination.PaginationService;
import com.upsjb.ms4.specification.VentaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VentaConsultaServiceImpl implements VentaConsultaService {

    private static final String RECURSO_VENTA = "Venta";

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final PagoRepository pagoRepository;
    private final BoletaRepository boletaRepository;
    private final VentaMapper ventaMapper;
    private final VentaDetalleMapper ventaDetalleMapper;
    private final PagoMapper pagoMapper;
    private final BoletaMapper boletaMapper;
    private final VentaPolicy ventaPolicy;
    private final PaginationService paginationService;

    public VentaConsultaServiceImpl(VentaRepository ventaRepository,
                                    VentaDetalleRepository ventaDetalleRepository,
                                    PagoRepository pagoRepository,
                                    BoletaRepository boletaRepository,
                                    VentaMapper ventaMapper,
                                    VentaDetalleMapper ventaDetalleMapper,
                                    PagoMapper pagoMapper,
                                    BoletaMapper boletaMapper,
                                    VentaPolicy ventaPolicy,
                                    PaginationService paginationService) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.pagoRepository = pagoRepository;
        this.boletaRepository = boletaRepository;
        this.ventaMapper = ventaMapper;
        this.ventaDetalleMapper = ventaDetalleMapper;
        this.pagoMapper = pagoMapper;
        this.boletaMapper = boletaMapper;
        this.ventaPolicy = ventaPolicy;
        this.paginationService = paginationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<VentaResponseDto> listarVentasAdmin(VentaFilterDto filter,
                                                               PageRequestDto page,
                                                               AuthenticatedUserContext actor) {
        ventaPolicy.authorizeListarVentasAdmin(actor);

        Page<Venta> result = ventaRepository.findAll(
                VentaSpecification.build(filter),
                paginationService.toPageable(page, "fechaVenta")
        );

        return paginationService.toPageResponse(result, ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<VentaResponseDto> listarVentasEmpleado(VentaFilterDto filter,
                                                                  PageRequestDto page,
                                                                  AuthenticatedUserContext actor) {
        ventaPolicy.authorizeListarMisVentasEmpleado(actor);

        VentaFilterDto securedFilter = actor != null && actor.isAdmin()
                ? filter
                : withEmpleadoUsuario(filter, actor.idUsuarioMs1());

        Specification<Venta> specification = VentaSpecification.build(securedFilter);

        Page<Venta> result = ventaRepository.findAll(
                specification,
                paginationService.toPageable(page, "fechaVenta")
        );

        return paginationService.toPageResponse(result, ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<VentaResponseDto> listarVentasCliente(VentaFilterDto filter,
                                                                 PageRequestDto page,
                                                                 AuthenticatedUserContext actor) {
        ventaPolicy.authorizeListarMisVentasCliente(actor);

        VentaFilterDto securedFilter = withClienteUsuario(filter, actor.idUsuarioMs1());

        Page<Venta> result = ventaRepository.findAll(
                VentaSpecification.build(securedFilter),
                paginationService.toPageable(page, "fechaVenta")
        );

        return paginationService.toPageResponse(result, ventaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDetailResponseDto obtenerDetalleAdmin(Long idVenta, AuthenticatedUserContext actor) {
        Venta venta = resolverVentaParaProcesoInterno(idVenta);
        ventaPolicy.authorizeListarVentasAdmin(actor);
        return construirDetalle(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDetailResponseDto obtenerDetalleEmpleado(Long idVenta, AuthenticatedUserContext actor) {
        Venta venta = resolverVentaParaProcesoInterno(idVenta);
        ventaPolicy.authorizeVerVenta(actor, venta);
        return construirDetalle(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDetailResponseDto obtenerDetalleCliente(Long idVenta, AuthenticatedUserContext actor) {
        Venta venta = resolverVentaParaProcesoInterno(idVenta);
        ventaPolicy.authorizeVerVenta(actor, venta);
        return construirDetalle(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public Venta resolverVentaParaProcesoInterno(Long idVenta) {
        if (idVenta == null || idVenta <= 0) {
            throw NotFoundException.byId(RECURSO_VENTA, idVenta);
        }

        return ventaRepository.findByIdAndEstadoTrue(idVenta)
                .orElseThrow(() -> NotFoundException.byId(RECURSO_VENTA, idVenta));
    }

    private VentaDetailResponseDto construirDetalle(Venta venta) {
        List<VentaDetalleResponseDto> detalles = ventaDetalleRepository
                .findByIdVentaAndEstadoTrueOrderByIdAsc(venta.getId())
                .stream()
                .map(ventaDetalleMapper::toResponse)
                .toList();

        List<PagoResponseDto> pagos = pagoRepository
                .findByIdVentaAndEstadoTrueOrderByCreatedAtDesc(venta.getId())
                .stream()
                .map(pagoMapper::toResponse)
                .toList();

        BoletaResponseDto boleta = boletaRepository.findByIdVentaAndEstadoTrue(venta.getId())
                .map(boletaMapper::toResponse)
                .orElse(null);

        return ventaMapper.toDetailResponse(venta, detalles, pagos, boleta);
    }

    private VentaFilterDto withClienteUsuario(VentaFilterDto filter, Long idUsuarioClienteMs1) {
        return new VentaFilterDto(
                filter == null ? null : filter.search(),
                filter == null ? null : filter.codigoVenta(),
                filter == null ? null : filter.canalVenta(),
                filter == null ? null : filter.estadoVenta(),
                filter == null ? null : filter.metodoPagoPrincipal(),
                filter == null ? null : filter.idClienteSnapshot(),
                filter == null ? null : filter.idClienteMs2(),
                idUsuarioClienteMs1,
                filter == null ? null : filter.idEmpleadoSnapshot(),
                filter == null ? null : filter.idEmpleadoMs2(),
                filter == null ? null : filter.idUsuarioEmpleadoMs1(),
                filter == null ? null : filter.idCaja(),
                true,
                filter == null ? null : filter.fechaDesde(),
                filter == null ? null : filter.fechaHasta()
        );
    }

    private VentaFilterDto withEmpleadoUsuario(VentaFilterDto filter, Long idUsuarioEmpleadoMs1) {
        return new VentaFilterDto(
                filter == null ? null : filter.search(),
                filter == null ? null : filter.codigoVenta(),
                filter == null ? null : filter.canalVenta(),
                filter == null ? null : filter.estadoVenta(),
                filter == null ? null : filter.metodoPagoPrincipal(),
                filter == null ? null : filter.idClienteSnapshot(),
                filter == null ? null : filter.idClienteMs2(),
                filter == null ? null : filter.idUsuarioClienteMs1(),
                filter == null ? null : filter.idEmpleadoSnapshot(),
                filter == null ? null : filter.idEmpleadoMs2(),
                idUsuarioEmpleadoMs1,
                filter == null ? null : filter.idCaja(),
                true,
                filter == null ? null : filter.fechaDesde(),
                filter == null ? null : filter.fechaHasta()
        );
    }
}
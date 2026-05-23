// ruta: src/main/java/com/upsjb/ms4/service/impl/lookup/LookupServiceImpl.java
package com.upsjb.ms4.service.impl.lookup;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.domain.enums.EstadoCaja;
import com.upsjb.ms4.dto.lookup.AlmacenLookupResponseDto;
import com.upsjb.ms4.dto.lookup.CajaAbiertaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.lookup.EmpleadoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.lookup.ProductoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SerieBoletaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SkuLookupResponseDto;
import com.upsjb.ms4.mapper.lookup.LookupMapper;
import com.upsjb.ms4.policy.LookupPolicy;
import com.upsjb.ms4.repository.CajaRepository;
import com.upsjb.ms4.repository.ClienteSnapshotMs2Repository;
import com.upsjb.ms4.repository.EmpleadoSnapshotMs2Repository;
import com.upsjb.ms4.repository.ProductoSnapshotMs3Repository;
import com.upsjb.ms4.repository.SerieBoletaRepository;
import com.upsjb.ms4.repository.SkuSnapshotMs3Repository;
import com.upsjb.ms4.repository.StockSnapshotMs3Repository;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import com.upsjb.ms4.service.contract.lookup.LookupService;
import com.upsjb.ms4.validator.LookupValidator;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LookupServiceImpl implements LookupService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    private final ClienteSnapshotMs2Repository clienteRepository;
    private final EmpleadoSnapshotMs2Repository empleadoRepository;
    private final ProductoSnapshotMs3Repository productoRepository;
    private final SkuSnapshotMs3Repository skuRepository;
    private final StockSnapshotMs3Repository stockRepository;
    private final SerieBoletaRepository serieBoletaRepository;
    private final CajaRepository cajaRepository;
    private final LookupMapper lookupMapper;
    private final LookupPolicy lookupPolicy;
    private final LookupValidator lookupValidator;
    private final Clock clock;

    public LookupServiceImpl(ClienteSnapshotMs2Repository clienteRepository,
                             EmpleadoSnapshotMs2Repository empleadoRepository,
                             ProductoSnapshotMs3Repository productoRepository,
                             SkuSnapshotMs3Repository skuRepository,
                             StockSnapshotMs3Repository stockRepository,
                             SerieBoletaRepository serieBoletaRepository,
                             CajaRepository cajaRepository,
                             LookupMapper lookupMapper,
                             LookupPolicy lookupPolicy,
                             LookupValidator lookupValidator,
                             Clock clock) {
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.productoRepository = productoRepository;
        this.skuRepository = skuRepository;
        this.stockRepository = stockRepository;
        this.serieBoletaRepository = serieBoletaRepository;
        this.cajaRepository = cajaRepository;
        this.lookupMapper = lookupMapper;
        this.lookupPolicy = lookupPolicy;
        this.lookupValidator = lookupValidator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteLookupResponseDto> clientes(LookupFilterDto filter, AuthenticatedUserContext actor) {
        lookupPolicy.authorizeLookupClientes(actor);
        lookupValidator.validarFiltro(filter);

        return clienteRepository.findAll(
                        clienteSpec(filter),
                        page(filter, "nombreCompleto")
                )
                .stream()
                .map(lookupMapper::toClienteLookup)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoLookupResponseDto> empleados(LookupFilterDto filter, AuthenticatedUserContext actor) {
        lookupPolicy.authorizeLookupEmpleados(actor);
        lookupValidator.validarFiltro(filter);

        return empleadoRepository.findAll(
                        empleadoSpec(filter),
                        page(filter, "codigoEmpleado")
                )
                .stream()
                .map(lookupMapper::toEmpleadoLookup)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoLookupResponseDto> productos(LookupFilterDto filter, AuthenticatedUserContext actor) {
        lookupPolicy.authorizeLookupCatalogo(actor);
        lookupValidator.validarFiltro(filter);

        return productoRepository.findAll(
                        productoSpec(filter),
                        page(filter, "nombre")
                )
                .stream()
                .map(lookupMapper::toProductoLookup)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkuLookupResponseDto> skus(LookupFilterDto filter, AuthenticatedUserContext actor) {
        lookupPolicy.authorizeLookupCatalogo(actor);
        lookupValidator.validarFiltro(filter);

        return skuRepository.findAll(
                        skuSpec(filter),
                        page(filter, "codigoSku")
                )
                .stream()
                .map(lookupMapper::toSkuLookup)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlmacenLookupResponseDto> almacenes(LookupFilterDto filter, AuthenticatedUserContext actor) {
        lookupPolicy.authorizeLookupCatalogo(actor);
        lookupValidator.validarFiltro(filter);

        Map<Long, AlmacenLookupResponseDto> almacenes = new LinkedHashMap<>();

        stockRepository.findAll(stockAlmacenSpec(filter), page(filter, "codigoAlmacen"))
                .stream()
                .map(lookupMapper::toAlmacenLookup)
                .forEach(item -> almacenes.putIfAbsent(item.idAlmacenMs3(), item));

        return new ArrayList<>(almacenes.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SerieBoletaLookupResponseDto> seriesBoleta(LookupFilterDto filter, AuthenticatedUserContext actor) {
        lookupPolicy.authorizeLookupSeriesBoleta(actor);
        lookupValidator.validarFiltro(filter);

        return serieBoletaRepository.findAll(
                        serieSpec(filter),
                        page(filter, "serie")
                )
                .stream()
                .map(lookupMapper::toSerieBoletaLookup)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CajaAbiertaLookupResponseDto cajaAbiertaHoy(AuthenticatedUserContext actor) {
        lookupPolicy.authorizeLookupCaja(actor);

        LocalDate today = LocalDate.now(clock);

        Caja caja = cajaRepository.findByFechaOperacionAndEstadoCajaAndEstadoTrue(today, EstadoCaja.ABIERTA)
                .orElse(null);

        return lookupMapper.toCajaAbiertaLookup(caja);
    }

    private Specification<ClienteSnapshotMs2> clienteSpec(LookupFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (soloActivos(filter)) {
                predicates.add(cb.isTrue(root.get("estado")));
                predicates.add(cb.isTrue(root.get("clienteActivoMs2")));
            }

            String search = like(filter);
            if (search != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombreCompleto")), search),
                        cb.like(cb.lower(root.get("razonSocial")), search),
                        cb.like(cb.lower(root.get("nombreComercial")), search),
                        cb.like(cb.lower(root.get("numeroDocumentoPersona")), search),
                        cb.like(cb.lower(root.get("ruc")), search),
                        cb.like(cb.lower(root.get("correoPrincipal")), search),
                        cb.like(cb.lower(root.get("telefonoPrincipal")), search)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<EmpleadoSnapshotMs2> empleadoSpec(LookupFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (soloActivos(filter)) {
                predicates.add(cb.isTrue(root.get("estado")));
                predicates.add(cb.isTrue(root.get("empleadoActivoMs2")));
            }

            String search = like(filter);
            if (search != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigoEmpleado")), search),
                        cb.like(cb.lower(root.get("nombreCompleto")), search),
                        cb.like(cb.lower(root.get("numeroDocumento")), search),
                        cb.like(cb.lower(root.get("correo")), search),
                        cb.like(cb.lower(root.get("areaNombre")), search)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<ProductoSnapshotMs3> productoSpec(LookupFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (soloActivos(filter)) {
                predicates.add(cb.isTrue(root.get("estado")));
                predicates.add(cb.isTrue(root.get("visiblePublico")));
                predicates.add(cb.isTrue(root.get("vendible")));
            }

            String search = like(filter);
            if (search != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigoProducto")), search),
                        cb.like(cb.lower(root.get("nombre")), search),
                        cb.like(cb.lower(root.get("slug")), search),
                        cb.like(cb.lower(root.get("nombreCategoria")), search),
                        cb.like(cb.lower(root.get("nombreMarca")), search)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<SkuSnapshotMs3> skuSpec(LookupFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (soloActivos(filter)) {
                predicates.add(cb.isTrue(root.get("estado")));
            }

            String search = like(filter);
            if (search != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigoSku")), search),
                        cb.like(cb.lower(root.get("codigoProducto")), search),
                        cb.like(cb.lower(root.get("barcode")), search),
                        cb.like(cb.lower(root.get("color")), search),
                        cb.like(cb.lower(root.get("talla")), search),
                        cb.like(cb.lower(root.get("modelo")), search)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<StockSnapshotMs3> stockAlmacenSpec(LookupFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (soloActivos(filter)) {
                predicates.add(cb.isTrue(root.get("estado")));
            }

            String search = like(filter);
            if (search != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigoAlmacen")), search),
                        cb.like(cb.lower(root.get("nombreAlmacen")), search),
                        cb.like(cb.lower(root.get("codigoSku")), search),
                        cb.like(cb.lower(root.get("codigoProducto")), search),
                        cb.like(cb.lower(root.get("nombreProducto")), search)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<SerieBoleta> serieSpec(LookupFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (soloActivos(filter)) {
                predicates.add(cb.isTrue(root.get("estado")));
            }

            String search = like(filter);
            if (search != null) {
                predicates.add(cb.like(cb.lower(root.get("serie")), search));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private PageRequest page(LookupFilterDto filter, String sortBy) {
        return PageRequest.of(
                0,
                limit(filter),
                Sort.by(Sort.Direction.ASC, sortBy)
        );
    }

    private int limit(LookupFilterDto filter) {
        if (filter == null || filter.limit() == null) {
            return DEFAULT_LIMIT;
        }

        return Math.min(Math.max(filter.limit(), 1), MAX_LIMIT);
    }

    private boolean soloActivos(LookupFilterDto filter) {
        return filter == null || filter.soloActivos() == null || Boolean.TRUE.equals(filter.soloActivos());
    }

    private String like(LookupFilterDto filter) {
        if (filter == null || filter.search() == null || filter.search().isBlank()) {
            return null;
        }

        return "%" + filter.search().trim().toLowerCase(Locale.ROOT) + "%";
    }
}
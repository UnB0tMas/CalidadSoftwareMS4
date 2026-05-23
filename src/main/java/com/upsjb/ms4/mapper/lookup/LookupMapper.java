// ruta: src/main/java/com/upsjb/ms4/mapper/lookup/LookupMapper.java
package com.upsjb.ms4.mapper.lookup;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.lookup.AlmacenLookupResponseDto;
import com.upsjb.ms4.dto.lookup.CajaAbiertaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.lookup.EmpleadoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupItemResponseDto;
import com.upsjb.ms4.dto.lookup.ProductoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SerieBoletaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SkuLookupResponseDto;
import org.springframework.stereotype.Component;

@Component
public class LookupMapper {

    public LookupItemResponseDto toResponse(Long id, String codigo, String label, String descripcion, Boolean activo) {
        return new LookupItemResponseDto(id, codigo, label, descripcion, activo);
    }

    public ClienteLookupResponseDto toClienteLookup(ClienteSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return new ClienteLookupResponseDto(
                entity.getId(),
                entity.getIdClienteMs2(),
                entity.getIdUsuarioMs1(),
                entity.getTipoCliente(),
                entity.getTipoDocumentoPersona(),
                entity.getNumeroDocumentoPersona(),
                entity.getNombreCompleto(),
                entity.getRazonSocial(),
                entity.getCorreoPrincipal(),
                entity.getTelefonoPrincipal(),
                entity.getDireccionPrincipal(),
                entity.getClienteActivoMs2(),
                entity.getEstado()
        );
    }

    public EmpleadoLookupResponseDto toEmpleadoLookup(EmpleadoSnapshotMs2 entity) {
        if (entity == null) {
            return null;
        }

        return new EmpleadoLookupResponseDto(
                entity.getId(),
                entity.getIdEmpleadoMs2(),
                entity.getIdUsuarioMs1(),
                entity.getCodigoEmpleado(),
                nombreEmpleado(entity),
                entity.getTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getCorreo(),
                entity.getAreaCodigo(),
                entity.getAreaNombre(),
                entity.getEmpleadoActivoMs2(),
                entity.getEstado()
        );
    }

    public ProductoLookupResponseDto toProductoLookup(ProductoSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new ProductoLookupResponseDto(
                entity.getId(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getNombre(),
                entity.getSlug(),
                entity.getNombreCategoria(),
                entity.getNombreMarca(),
                entity.getEstadoPublicacion(),
                entity.getEstadoVenta(),
                entity.getVisiblePublico(),
                entity.getVendible(),
                entity.getEstado()
        );
    }

    public SkuLookupResponseDto toSkuLookup(SkuSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new SkuLookupResponseDto(
                entity.getId(),
                entity.getIdSkuMs3(),
                entity.getIdProductoMs3(),
                entity.getCodigoProducto(),
                entity.getCodigoSku(),
                entity.getBarcode(),
                entity.getColor(),
                entity.getTalla(),
                entity.getMaterial(),
                entity.getModelo(),
                entity.getEstadoSku(),
                entity.getEstado()
        );
    }

    public AlmacenLookupResponseDto toAlmacenLookup(StockSnapshotMs3 entity) {
        if (entity == null) {
            return null;
        }

        return new AlmacenLookupResponseDto(
                entity.getIdAlmacenMs3(),
                entity.getCodigoAlmacen(),
                entity.getNombreAlmacen(),
                disponibleParaVenta(entity)
        );
    }

    public SerieBoletaLookupResponseDto toSerieBoletaLookup(SerieBoleta entity) {
        if (entity == null) {
            return null;
        }

        return new SerieBoletaLookupResponseDto(
                entity.getId(),
                entity.getSerie(),
                entity.getNumeroActual(),
                entity.getNumeroInicio(),
                entity.getNumeroFin(),
                entity.getEstado()
        );
    }

    public CajaAbiertaLookupResponseDto toCajaAbiertaLookup(Caja entity) {
        if (entity == null) {
            return null;
        }

        return new CajaAbiertaLookupResponseDto(
                entity.getId(),
                entity.getCodigoCaja(),
                entity.getFechaOperacion(),
                entity.getEstadoCaja(),
                entity.getMontoInicial(),
                entity.getMontoEsperadoEfectivo(),
                entity.getMontoTarjeta(),
                entity.getMontoTotalVendido(),
                entity.getIdEmpleadoAperturaSnapshot(),
                entity.getIdUsuarioAperturaMs1(),
                nombreEmpleado(entity.getEmpleadoAperturaSnapshot()),
                entity.getFechaApertura()
        );
    }

    private Boolean disponibleParaVenta(StockSnapshotMs3 entity) {
        return Boolean.TRUE.equals(entity.getEstado())
                && entity.getStockDisponible() != null
                && entity.getStockDisponible() > 0;
    }

    private String nombreEmpleado(EmpleadoSnapshotMs2 empleado) {
        if (empleado == null) {
            return null;
        }

        return firstNonBlank(
                empleado.getNombreCompleto(),
                joinNombre(empleado.getNombres(), empleado.getApePaterno(), empleado.getApeMaterno()),
                empleado.getCodigoEmpleado()
        );
    }

    private String joinNombre(String nombres, String apePaterno, String apeMaterno) {
        StringBuilder builder = new StringBuilder();

        append(builder, nombres);
        append(builder, apePaterno);
        append(builder, apeMaterno);

        return builder.isEmpty() ? null : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(' ');
        }

        builder.append(value.trim());
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }
}
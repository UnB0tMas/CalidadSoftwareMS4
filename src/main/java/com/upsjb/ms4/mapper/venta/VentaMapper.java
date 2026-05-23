// ruta: src/main/java/com/upsjb/ms4/mapper/venta/VentaMapper.java
package com.upsjb.ms4.mapper.venta;

import com.upsjb.ms4.domain.entity.snapshot.ClienteSnapshotMs2;
import com.upsjb.ms4.domain.entity.snapshot.EmpleadoSnapshotMs2;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.dto.boleta.response.BoletaResponseDto;
import com.upsjb.ms4.dto.pago.response.PagoResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetailResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaDetalleResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResponseDto;
import com.upsjb.ms4.dto.venta.response.VentaResumenResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VentaMapper {

    public VentaResponseDto toResponse(Venta entity) {
        if (entity == null) {
            return null;
        }

        String clienteNombre = nombreCliente(entity.getClienteSnapshot());
        String clienteDocumento = documentoCliente(entity.getClienteSnapshot());
        String empleadoNombre = nombreEmpleado(entity.getEmpleadoSnapshot());
        String codigoCaja = entity.getCaja() != null ? entity.getCaja().getCodigoCaja() : null;

        return new VentaResponseDto(
                entity.getId(),
                entity.getCodigoVenta(),
                entity.getCanalVenta(),
                entity.getEstadoVenta(),
                entity.getIdClienteSnapshot(),
                entity.getIdClienteMs2(),
                entity.getIdUsuarioClienteMs1(),
                clienteNombre,
                clienteDocumento,
                entity.getIdEmpleadoSnapshot(),
                entity.getIdEmpleadoMs2(),
                entity.getIdUsuarioEmpleadoMs1(),
                empleadoNombre,
                entity.getIdCaja(),
                codigoCaja,
                entity.getIdConfiguracionTributariaVersion(),
                entity.getMoneda(),
                entity.getSubtotal(),
                entity.getDescuentoTotal(),
                entity.getOpGravada(),
                entity.getOpExonerada(),
                entity.getOpInafecta(),
                entity.getIgvPorcentaje(),
                entity.getIgvTotal(),
                entity.getTotal(),
                entity.getMetodoPagoPrincipal(),
                entity.getFechaVenta(),
                entity.getObservacion(),
                entity.getRequestId(),
                entity.getCorrelationId(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public VentaDetailResponseDto toDetailResponse(
            Venta entity,
            List<VentaDetalleResponseDto> detalles,
            List<PagoResponseDto> pagos,
            BoletaResponseDto boleta
    ) {
        return new VentaDetailResponseDto(
                toResponse(entity),
                detalles == null ? List.of() : detalles,
                pagos == null ? List.of() : pagos,
                boleta
        );
    }

    public VentaResumenResponseDto toResumenResponse(Venta entity) {
        if (entity == null) {
            return null;
        }

        return new VentaResumenResponseDto(
                entity.getId(),
                entity.getCodigoVenta(),
                entity.getCanalVenta(),
                entity.getEstadoVenta(),
                nombreCliente(entity.getClienteSnapshot()),
                nombreEmpleado(entity.getEmpleadoSnapshot()),
                entity.getMetodoPagoPrincipal(),
                entity.getMoneda(),
                entity.getTotal(),
                entity.getFechaVenta()
        );
    }

    private String nombreCliente(ClienteSnapshotMs2 cliente) {
        if (cliente == null) {
            return null;
        }

        return firstNonBlank(
                cliente.getNombreCompleto(),
                cliente.getRazonSocial(),
                cliente.getNombreComercial()
        );
    }

    private String documentoCliente(ClienteSnapshotMs2 cliente) {
        if (cliente == null) {
            return null;
        }

        return firstNonBlank(
                cliente.getNumeroDocumentoPersona(),
                cliente.getRuc()
        );
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
// ruta: src/main/java/com/upsjb/ms4/validator/VentaDetalleValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.snapshot.PrecioSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.ProductoSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.SkuSnapshotMs3;
import com.upsjb.ms4.domain.entity.snapshot.StockSnapshotMs3;
import com.upsjb.ms4.dto.venta.request.VentaDetalleRequestDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class VentaDetalleValidator extends ValidatorSupport {

    public void validarDetallesNoVacios(List<VentaDetalleRequestDto> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            fail("La venta debe contener al menos un detalle.");
        }

        if (detalles.size() > 100) {
            fail("La venta no puede superar 100 detalles.");
        }
    }

    public void validarCantidadPositiva(Integer cantidad) {
        requirePositive(cantidad, "La cantidad");
    }

    public void validarSkuNoDuplicado(List<VentaDetalleRequestDto> detalles) {
        validarDetallesNoVacios(detalles);

        Set<String> keys = new HashSet<>();

        for (VentaDetalleRequestDto item : detalles) {
            validarDetalleRequest(item);

            String skuKey = item.idSkuMs3() != null
                    ? "IDSKU-" + item.idSkuMs3()
                    : "CODSKU-" + normalize(item.codigoSku(), item.barcode());

            String almacenKey = item.idAlmacenMs3() != null
                    ? "IDALM-" + item.idAlmacenMs3()
                    : "CODALM-" + normalize(item.codigoAlmacen(), null);

            String key = skuKey + "-" + almacenKey;

            if (!keys.add(key)) {
                conflict("No se permite repetir el mismo SKU en el mismo almacén dentro de la venta.");
            }
        }
    }

    public void validarAlmacenObligatorio(Long idAlmacenMs3, String codigoAlmacen) {
        if ((idAlmacenMs3 == null || idAlmacenMs3 <= 0) && isBlank(codigoAlmacen)) {
            fail("Debe indicar el almacén por id o código.");
        }

        if (idAlmacenMs3 != null) {
            requirePositive(idAlmacenMs3, "El almacén");
        }
    }

    public void validarAlmacenObligatorio(Long idAlmacenMs3) {
        validarAlmacenObligatorio(idAlmacenMs3, null);
    }

    public void validarDetalleVendible(ProductoSnapshotMs3 producto,
                                       SkuSnapshotMs3 sku,
                                       PrecioSnapshotMs3 precio,
                                       StockSnapshotMs3 stock,
                                       Integer cantidad) {
        require(producto, "El producto snapshot es obligatorio.");
        require(sku, "El SKU snapshot es obligatorio.");
        require(precio, "El precio snapshot es obligatorio.");
        require(stock, "El stock snapshot es obligatorio.");
        validarCantidadPositiva(cantidad);

        requireActive(producto.getEstado(), "El producto no se encuentra activo.");
        requireActive(producto.getVendible(), "El producto no se encuentra vendible.");
        requireActive(sku.getEstado(), "El SKU no se encuentra activo.");
        requireActive(precio.getEstado(), "El precio snapshot no se encuentra activo.");
        requireActive(precio.getVigente(), "El SKU no tiene precio vigente.");
        requireActive(stock.getEstado(), "El stock snapshot no se encuentra activo.");

        if (producto.getIdProductoMs3() == null || sku.getIdProductoMs3() == null) {
            fail("El producto o SKU snapshot no tiene identificadores MS3 completos.");
        }

        if (!producto.getIdProductoMs3().equals(sku.getIdProductoMs3())) {
            fail("El SKU no pertenece al producto snapshot indicado.");
        }

        if (!sku.getIdSkuMs3().equals(precio.getIdSkuMs3())) {
            fail("El precio vigente no corresponde al SKU indicado.");
        }

        if (!sku.getIdSkuMs3().equals(stock.getIdSkuMs3())) {
            fail("El stock snapshot no corresponde al SKU indicado.");
        }

        requirePositive(precio.getPrecioVenta(), "El precio de venta");
    }

    public void validarDetalleRequest(VentaDetalleRequestDto detalle) {
        require(detalle, "El detalle de venta es obligatorio.");

        if ((detalle.idSkuMs3() == null || detalle.idSkuMs3() <= 0)
                && isBlank(detalle.codigoSku())
                && isBlank(detalle.barcode())) {
            fail("Debe indicar el SKU por id, código SKU o código de barras.");
        }

        if (detalle.idSkuMs3() != null) {
            requirePositive(detalle.idSkuMs3(), "El SKU");
        }

        validarAlmacenObligatorio(detalle.idAlmacenMs3(), detalle.codigoAlmacen());
        validarCantidadPositiva(detalle.cantidad());
    }

    private String normalize(String primary, String fallback) {
        String value = !isBlank(primary) ? primary : fallback;
        return value == null ? "SIN_VALOR" : value.trim().toUpperCase();
    }
}
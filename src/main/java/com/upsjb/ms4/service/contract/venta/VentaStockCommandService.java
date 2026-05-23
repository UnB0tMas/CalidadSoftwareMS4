// ruta: src/main/java/com/upsjb/ms4/service/contract/venta/VentaStockCommandService.java
package com.upsjb.ms4.service.contract.venta;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import com.upsjb.ms4.dto.kafka.ms4.Ms4StockCommandEventDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface VentaStockCommandService {

    void registrarComandosConfirmacionStock(Venta venta, AuthenticatedUserContext actor);

    void registrarComandosAnulacionStock(Venta venta, AuthenticatedUserContext actor);

    void registrarComandoLiberacionReserva(Venta venta, AuthenticatedUserContext actor);

    Ms4StockCommandEventDto construirComandoStock(Venta venta,
                                                  VentaDetalle detalle,
                                                  TipoComandoStock tipoComando);

    String generarIdempotencyKey(Venta venta,
                                 VentaDetalle detalle,
                                 TipoComandoStock tipoComando);

    void registrarEventoPendienteSiContingencia(Venta venta,
                                                VentaDetalle detalle,
                                                TipoComandoStock tipoComando);

    void registrarOutboxStockCommand(Ms4StockCommandEventDto event,
                                     AuthenticatedUserContext actor);
}
// ruta: src/main/java/com/upsjb/ms4/service/contract/caja/CajaMovimientoService.java
package com.upsjb.ms4.service.contract.caja;

import com.upsjb.ms4.domain.entity.caja.Caja;
import com.upsjb.ms4.domain.entity.pago.Pago;
import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.dto.caja.filter.CajaMovimientoFilterDto;
import com.upsjb.ms4.dto.caja.request.CajaAjusteRequestDto;
import com.upsjb.ms4.dto.caja.response.CajaMovimientoResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface CajaMovimientoService {

    CajaMovimientoResponseDto registrarMovimientoApertura(Caja caja, AuthenticatedUserContext actor);

    CajaMovimientoResponseDto registrarMovimientoVentaEfectivo(Caja caja,
                                                               Venta venta,
                                                               Pago pago,
                                                               AuthenticatedUserContext actor);

    CajaMovimientoResponseDto registrarMovimientoVentaTarjeta(Caja caja,
                                                              Venta venta,
                                                              Pago pago,
                                                              AuthenticatedUserContext actor);

    CajaMovimientoResponseDto registrarMovimientoCierre(Caja caja, AuthenticatedUserContext actor);

    CajaMovimientoResponseDto registrarMovimientoAjuste(Caja caja,
                                                        CajaAjusteRequestDto request,
                                                        AuthenticatedUserContext actor);

    PageResponseDto<CajaMovimientoResponseDto> listarMovimientos(Long idCaja,
                                                                 CajaMovimientoFilterDto filter,
                                                                 PageRequestDto page);
}
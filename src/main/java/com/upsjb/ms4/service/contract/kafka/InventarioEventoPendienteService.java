// ruta: src/main/java/com/upsjb/ms4/service/contract/kafka/InventarioEventoPendienteService.java
package com.upsjb.ms4.service.contract.kafka;

import com.upsjb.ms4.domain.entity.venta.Venta;
import com.upsjb.ms4.domain.entity.venta.VentaDetalle;
import com.upsjb.ms4.domain.enums.TipoComandoStock;
import com.upsjb.ms4.dto.contingencia.filter.InventarioEventoPendienteFilterDto;
import com.upsjb.ms4.dto.contingencia.request.StockSyncResultRequestDto;
import com.upsjb.ms4.dto.contingencia.response.ContingenciaReconciliacionResponseDto;
import com.upsjb.ms4.dto.contingencia.response.InventarioEventoPendienteResponseDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

public interface InventarioEventoPendienteService {

    InventarioEventoPendienteResponseDto registrarPendiente(Venta venta,
                                                            VentaDetalle detalle,
                                                            TipoComandoStock tipoComando,
                                                            String payloadJson);

    PageResponseDto<InventarioEventoPendienteResponseDto> listar(InventarioEventoPendienteFilterDto filter,
                                                                 PageRequestDto page);

    PageResponseDto<InventarioEventoPendienteResponseDto> listarPendientesParaMs3(InventarioEventoPendienteFilterDto filter,
                                                                                  PageRequestDto page);

    InventarioEventoPendienteResponseDto marcarSincronizado(Long idPendiente, String detalleResultado);

    InventarioEventoPendienteResponseDto marcarError(Long idPendiente, String errorDetalle);

    InventarioEventoPendienteResponseDto registrarResultadoSincronizacion(StockSyncResultRequestDto request);

    InventarioEventoPendienteResponseDto reintentar(Long idPendiente, AuthenticatedUserContext actor);

    ContingenciaReconciliacionResponseDto reconciliarPendientes(AuthenticatedUserContext actor);

    long contarPendientesActivos();
}
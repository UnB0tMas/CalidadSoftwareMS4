// ruta: src/main/java/com/upsjb/ms4/service/contract/lookup/LookupService.java
package com.upsjb.ms4.service.contract.lookup;

import com.upsjb.ms4.dto.lookup.AlmacenLookupResponseDto;
import com.upsjb.ms4.dto.lookup.CajaAbiertaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.ClienteLookupResponseDto;
import com.upsjb.ms4.dto.lookup.EmpleadoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.LookupFilterDto;
import com.upsjb.ms4.dto.lookup.ProductoLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SerieBoletaLookupResponseDto;
import com.upsjb.ms4.dto.lookup.SkuLookupResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;

import java.util.List;

public interface LookupService {

    List<ClienteLookupResponseDto> clientes(LookupFilterDto filter, AuthenticatedUserContext actor);

    List<EmpleadoLookupResponseDto> empleados(LookupFilterDto filter, AuthenticatedUserContext actor);

    List<ProductoLookupResponseDto> productos(LookupFilterDto filter, AuthenticatedUserContext actor);

    List<SkuLookupResponseDto> skus(LookupFilterDto filter, AuthenticatedUserContext actor);

    List<AlmacenLookupResponseDto> almacenes(LookupFilterDto filter, AuthenticatedUserContext actor);

    List<SerieBoletaLookupResponseDto> seriesBoleta(LookupFilterDto filter, AuthenticatedUserContext actor);

    CajaAbiertaLookupResponseDto cajaAbiertaHoy(AuthenticatedUserContext actor);
}
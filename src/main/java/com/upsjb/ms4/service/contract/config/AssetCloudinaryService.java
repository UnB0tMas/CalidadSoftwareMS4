// ruta: src/main/java/com/upsjb/ms4/service/contract/config/AssetCloudinaryService.java
package com.upsjb.ms4.service.contract.config;

import com.upsjb.ms4.dto.config.filter.AssetCloudinaryFilterDto;
import com.upsjb.ms4.dto.config.request.AssetCloudinaryUploadRequestDto;
import com.upsjb.ms4.dto.config.response.AssetCloudinaryResponseDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import com.upsjb.ms4.dto.shared.PageRequestDto;
import com.upsjb.ms4.dto.shared.PageResponseDto;
import com.upsjb.ms4.security.principal.AuthenticatedUserContext;
import org.springframework.web.multipart.MultipartFile;

public interface AssetCloudinaryService {

    AssetCloudinaryResponseDto subirAssetVisual(AssetCloudinaryUploadRequestDto request,
                                                MultipartFile file,
                                                AuthenticatedUserContext actor);

    AssetCloudinaryResponseDto reemplazarAssetVisual(Long idAssetActual,
                                                     AssetCloudinaryUploadRequestDto request,
                                                     MultipartFile file,
                                                     AuthenticatedUserContext actor);

    AssetCloudinaryResponseDto obtenerPorId(Long idAsset);

    PageResponseDto<AssetCloudinaryResponseDto> listar(AssetCloudinaryFilterDto filter, PageRequestDto page);

    AssetCloudinaryResponseDto cambiarEstado(Long idAsset,
                                             EstadoChangeRequestDto request,
                                             AuthenticatedUserContext actor);

    void validarAssetVisualPermitido(MultipartFile file, String tipoAsset);
}
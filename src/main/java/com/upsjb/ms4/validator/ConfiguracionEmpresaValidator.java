package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.config.AssetCloudinary;
import com.upsjb.ms4.domain.entity.config.ConfiguracionEmpresaVersion;
import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import com.upsjb.ms4.dto.config.filter.ConfiguracionEmpresaFilterDto;
import com.upsjb.ms4.dto.config.request.ConfiguracionEmpresaRequestDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Component
public class ConfiguracionEmpresaValidator extends ValidatorSupport {

    private static final Pattern RUC_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern COLOR_HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

    public void validarNuevaVersion(ConfiguracionEmpresaRequestDto request, AssetCloudinary logoAsset) {
        require(request, "La configuración de empresa es obligatoria.");

        validarRuc(request.ruc());
        requireText(request.razonSocial(), "La razón social es obligatoria.");
        requireMaxLength(request.razonSocial(), 250, "La razón social");
        requireText(request.direccionFiscal(), "La dirección fiscal es obligatoria.");
        requireMaxLength(request.direccionFiscal(), 500, "La dirección fiscal");
        requireText(request.motivo(), "El motivo de la nueva versión empresarial es obligatorio.");
        requireMaxLength(request.motivo(), 500, "El motivo");

        validarTextoOpcional(request.nombreComercial(), 250, "El nombre comercial");
        validarTextoOpcional(request.telefono(), 50, "El teléfono");
        validarTextoOpcional(request.web(), 250, "La web");
        validarTextoOpcional(request.mensajePieBoleta(), 1000, "El mensaje de pie de boleta");
        validarTextoOpcional(request.terminosCondiciones(), 10000, "Los términos y condiciones");
        validarTextoOpcional(request.politicaCambios(), 10000, "La política de cambios");
        validarColorOpcional(request.colorPrimario(), "El color primario");
        validarColorOpcional(request.colorSecundario(), "El color secundario");
        validarVigencias(request.fechaInicioVigencia(), null);
        validarAssetLogo(request.idLogoAsset(), logoAsset);

        if (!isBlank(request.correo())) {
            requireEmail(request.correo(), "El correo de empresa");
            requireMaxLength(request.correo(), 180, "El correo de empresa");
        }
    }

    public void validarFiltro(ConfiguracionEmpresaFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");

        if (!isBlank(filter.ruc())) {
            validarRuc(filter.ruc());
        }

        validarVigencias(filter.fechaInicioDesde(), filter.fechaInicioHasta());
    }

    public void validarCambioEstado(ConfiguracionEmpresaVersion version, EstadoChangeRequestDto request) {
        require(version, "La versión empresarial es obligatoria.");
        require(request, "La solicitud de cambio de estado es obligatoria.");
        require(request.estado(), "El estado es obligatorio.");
        requireText(request.motivo(), "El motivo es obligatorio para cambiar el estado.");
        requireMaxLength(request.motivo(), 500, "El motivo");
    }

    public void validarActivacion(ConfiguracionEmpresaVersion version, AssetCloudinary logoAsset) {
        require(version, "La versión empresarial es obligatoria.");

        if (!Boolean.TRUE.equals(version.getEstado())) {
            conflict("La versión empresarial no está activa.");
        }

        if (Boolean.TRUE.equals(version.getVigente())) {
            conflict("La versión empresarial ya se encuentra vigente.");
        }

        validarAssetLogo(version.getIdLogoAsset(), logoAsset);
    }

    public void validarRuc(String ruc) {
        if (ruc == null || !RUC_PATTERN.matcher(ruc.trim()).matches()) {
            fail("El RUC debe contener 11 dígitos.");
        }
    }

    public void validarVigencias(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        requireDateRange(fechaInicio, fechaFin, "La vigencia empresarial");
    }

    public void validarAssetLogo(Long idLogoAsset, AssetCloudinary logoAsset) {
        if (idLogoAsset == null) {
            return;
        }

        require(logoAsset, "El asset de logo indicado no existe.");
        requireActive(logoAsset.getEstado(), "El asset de logo no está activo.");

        if (logoAsset.getResourceType() != ResourceTypeCloudinary.IMAGE) {
            fail("El logo de empresa debe ser un asset Cloudinary de tipo IMAGE.");
        }

        requireText(logoAsset.getSecureUrl(), "El asset de logo no tiene secureUrl.");
        requireText(logoAsset.getPublicId(), "El asset de logo no tiene publicId.");
    }

    private void validarTextoOpcional(String value, int maxLength, String fieldName) {
        requireMaxLength(value, maxLength, fieldName);
    }

    private void validarColorOpcional(String value, String fieldName) {
        if (isBlank(value)) {
            return;
        }

        requireMaxLength(value, 20, fieldName);

        if (!COLOR_HEX_PATTERN.matcher(value.trim()).matches()) {
            fail(fieldName + " debe tener formato hexadecimal, por ejemplo #0D6EFD.");
        }
    }
}
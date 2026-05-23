// ruta: src/main/java/com/upsjb/ms4/validator/AssetCloudinaryValidator.java
package com.upsjb.ms4.validator;

import com.upsjb.ms4.domain.entity.config.AssetCloudinary;
import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import com.upsjb.ms4.dto.config.filter.AssetCloudinaryFilterDto;
import com.upsjb.ms4.dto.config.request.AssetCloudinaryUploadRequestDto;
import com.upsjb.ms4.dto.shared.EstadoChangeRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AssetCloudinaryValidator extends ValidatorSupport {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "webp"
    );

    private static final Pattern CODIGO_MAYUSCULA_PATTERN = Pattern.compile("^[A-Z0-9_]+$");
    private static final Pattern FOLDER_PATTERN = Pattern.compile("^[a-zA-Z0-9/_-]+$");

    public void validarUpload(AssetCloudinaryUploadRequestDto request, MultipartFile file) {
        require(request, "La solicitud de asset visual es obligatoria.");

        validarCodigoMayuscula(request.entidadOrigen(), 80, "La entidad origen");
        validarCodigoMayuscula(request.tipoAsset(), 60, "El tipo de asset");
        validarFolder(request.folder());
        require(request.resourceType(), "El resource type es obligatorio.");

        if (request.resourceType() != ResourceTypeCloudinary.IMAGE) {
            fail("MS4 solo permite subir assets visuales de tipo IMAGE.");
        }

        validarAssetVisualPermitido(file, request.tipoAsset());
    }

    public void validarAssetVisualPermitido(MultipartFile file, String tipoAsset) {
        validarCodigoMayuscula(tipoAsset, 60, "El tipo de asset");

        if (file == null || file.isEmpty()) {
            fail("El archivo del asset visual es obligatorio.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            fail("El asset visual no debe superar 5 MB.");
        }

        String contentType = normalizeLower(file.getContentType());
        String filename = normalizeLower(file.getOriginalFilename());
        String extension = resolverExtension(filename);

        if ("application/pdf".equals(contentType) || "pdf".equals(extension)) {
            fail("MS4 no permite subir PDFs de boleta a Cloudinary.");
        }

        if ("image/svg+xml".equals(contentType) || "svg".equals(extension)) {
            fail("MS4 no permite subir SVG como asset visual por seguridad.");
        }

        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            fail("Cloudinary en MS4 solo acepta imágenes JPEG, PNG o WEBP.");
        }

        if (extension != null && !EXTENSIONES_PERMITIDAS.contains(extension)) {
            fail("La extensión del asset visual debe ser JPG, JPEG, PNG o WEBP.");
        }
    }

    public void validarCambioEstado(AssetCloudinary asset, EstadoChangeRequestDto request) {
        require(asset, "El asset visual es obligatorio.");
        require(request, "La solicitud de cambio de estado es obligatoria.");
        require(request.estado(), "El estado es obligatorio.");
        requireText(request.motivo(), "El motivo es obligatorio para cambiar el estado.");
        requireMaxLength(request.motivo(), 500, "El motivo");
    }

    public void validarFiltro(AssetCloudinaryFilterDto filter) {
        if (filter == null) {
            return;
        }

        requireMaxLength(filter.search(), 150, "El texto de búsqueda");
        requireMaxLength(filter.entidadOrigen(), 80, "La entidad origen");
        requireMaxLength(filter.tipoAsset(), 60, "El tipo de asset");
        requireMaxLength(filter.folder(), 255, "La carpeta");

        if (!isBlank(filter.entidadOrigen()) && !CODIGO_MAYUSCULA_PATTERN.matcher(filter.entidadOrigen().trim()).matches()) {
            fail("La entidad origen solo debe contener mayúsculas, números y guion bajo.");
        }

        if (!isBlank(filter.tipoAsset()) && !CODIGO_MAYUSCULA_PATTERN.matcher(filter.tipoAsset().trim()).matches()) {
            fail("El tipo de asset solo debe contener mayúsculas, números y guion bajo.");
        }

        if (!isBlank(filter.folder()) && !FOLDER_PATTERN.matcher(filter.folder().trim()).matches()) {
            fail("La carpeta solo puede contener letras, números, slash, guion y guion bajo.");
        }
    }

    private void validarCodigoMayuscula(String value, int maxLength, String fieldName) {
        requireText(value, fieldName + " es obligatorio.");
        requireMaxLength(value, maxLength, fieldName);

        if (!CODIGO_MAYUSCULA_PATTERN.matcher(value.trim()).matches()) {
            fail(fieldName + " solo debe contener mayúsculas, números y guion bajo.");
        }
    }

    private void validarFolder(String folder) {
        requireText(folder, "La carpeta Cloudinary es obligatoria.");
        requireMaxLength(folder, 255, "La carpeta");

        String normalized = folder.trim();

        if (!FOLDER_PATTERN.matcher(normalized).matches()) {
            fail("La carpeta solo puede contener letras, números, slash, guion y guion bajo.");
        }

        if (normalized.contains("..")) {
            fail("La carpeta Cloudinary no puede contener rutas relativas.");
        }

        if (normalized.startsWith("/") || normalized.endsWith("/")) {
            fail("La carpeta Cloudinary no debe iniciar ni terminar con slash.");
        }
    }

    private String resolverExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }

        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return null;
        }

        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeLower(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
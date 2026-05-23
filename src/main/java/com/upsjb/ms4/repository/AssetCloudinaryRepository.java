// ruta: src/main/java/com/upsjb/ms4/repository/AssetCloudinaryRepository.java
package com.upsjb.ms4.repository;

import com.upsjb.ms4.domain.entity.config.AssetCloudinary;
import com.upsjb.ms4.domain.enums.ResourceTypeCloudinary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AssetCloudinaryRepository extends
        JpaRepository<AssetCloudinary, Long>,
        JpaSpecificationExecutor<AssetCloudinary> {

    Optional<AssetCloudinary> findByPublicId(String publicId);

    Optional<AssetCloudinary> findByPublicIdAndEstadoTrue(String publicId);

    boolean existsByPublicId(String publicId);

    boolean existsByHashSha256AndEstadoTrue(String hashSha256);

    boolean existsByHashSha256AndEstadoTrueAndIdNot(String hashSha256, Long id);

    Optional<AssetCloudinary> findFirstByHashSha256AndEstadoTrue(String hashSha256);

    List<AssetCloudinary> findByEntidadOrigenAndIdEntidadOrigenAndEstadoTrueOrderByCreatedAtDesc(
            String entidadOrigen,
            Long idEntidadOrigen
    );

    Optional<AssetCloudinary> findFirstByEntidadOrigenAndIdEntidadOrigenAndTipoAssetAndEstadoTrueOrderByCreatedAtDesc(
            String entidadOrigen,
            Long idEntidadOrigen,
            String tipoAsset
    );

    Page<AssetCloudinary> findByResourceTypeAndEstadoTrue(ResourceTypeCloudinary resourceType, Pageable pageable);

    Page<AssetCloudinary> findByEstadoTrue(Pageable pageable);
}
// ruta: src/main/java/com/upsjb/ms4/mapper/config/SerieBoletaMapper.java
package com.upsjb.ms4.mapper.config;

import com.upsjb.ms4.domain.entity.config.SerieBoleta;
import com.upsjb.ms4.dto.config.request.SerieBoletaCreateRequestDto;
import com.upsjb.ms4.dto.config.response.SerieBoletaResponseDto;
import com.upsjb.ms4.dto.lookup.SerieBoletaLookupResponseDto;
import org.springframework.stereotype.Component;

@Component
public class SerieBoletaMapper {

    public SerieBoletaResponseDto toResponse(SerieBoleta entity) {
        if (entity == null) return null;

        return new SerieBoletaResponseDto(
                entity.getId(),
                entity.getSerie(),
                entity.getNumeroActual(),
                entity.getNumeroInicio(),
                entity.getNumeroFin(),
                entity.getCreadoPorIdUsuarioMs1(),
                entity.getEstado(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public SerieBoletaResponseDto toDetailResponse(SerieBoleta entity) {
        return toResponse(entity);
    }

    public SerieBoletaLookupResponseDto toLookup(SerieBoleta entity) {
        if (entity == null) return null;

        return new SerieBoletaLookupResponseDto(
                entity.getId(),
                entity.getSerie(),
                entity.getNumeroActual(),
                entity.getNumeroInicio(),
                entity.getNumeroFin(),
                entity.getEstado()
        );
    }

    public SerieBoleta toEntity(SerieBoletaCreateRequestDto request) {
        if (request == null) return null;

        SerieBoleta entity = new SerieBoleta();
        entity.setSerie(request.serie());
        entity.setNumeroActual(request.numeroInicio() - 1);
        entity.setNumeroInicio(request.numeroInicio());
        entity.setNumeroFin(request.numeroFin());
        return entity;
    }
}
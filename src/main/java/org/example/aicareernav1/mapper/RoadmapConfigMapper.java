package org.example.aicareernav1.mapper;

import org.example.aicareernav1.dto.roadmap.config.ConfigUpdateDto;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoadmapConfigMapper {

    /**
     * Обновляет существующую сущность RoadmapConfig данными из DTO.
     * Поля в Entity изменятся только если в DTO они не null.
     */
    void updateEntityFromDto(ConfigUpdateDto dto, @MappingTarget RoadmapConfig entity);
}

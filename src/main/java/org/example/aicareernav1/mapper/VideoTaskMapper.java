package org.example.aicareernav1.mapper;

import org.example.aicareernav1.dto.VideoTaskDTO;
import org.example.aicareernav1.entity.VideoTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
  componentModel = "spring",
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VideoTaskMapper {

  // MapStruct сам поймет, как преобразовать VideoStatus в String и обратно
  VideoTaskDTO toDto(VideoTask entity);

  @Mapping(target = "createdAt", ignore = true)
  VideoTask toEntity(VideoTaskDTO dto);
}

package org.example.aicareernav1.mapper;

import org.example.aicareernav1.dto.roadmap.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContentMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "checkpoint", ignore = true)
  Module toEntity(ModuleDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "module", ignore = true)
  @Mapping(target = "theory", source = "theory")
  Lesson toEntity(LessonDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "lesson", ignore = true)
  Theory toEntity(TheoryDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "theory", ignore = true)
  Resource toEntity(ResourceDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "lesson", ignore = true)
  @Mapping(target = "content", expression = "java(dto.getContent().toString())")
  Task toEntity(TaskDTO dto);

  @AfterMapping
  default void linkRelations(@MappingTarget Module module) {
    if (module.getLessons() != null) {
      module.getLessons().forEach(lesson -> {
        lesson.setModule(module);
        if (lesson.getTheory() != null) {
          lesson.getTheory().setLesson(lesson);
          if (lesson.getTheory().getResources() != null) {
            lesson.getTheory().getResources().forEach(r -> r.setTheory(lesson.getTheory()));
          }
        }
        if (lesson.getTasks() != null) {
          lesson.getTasks().forEach(t -> t.setLesson(lesson));
        }
      });
    }
  }
}

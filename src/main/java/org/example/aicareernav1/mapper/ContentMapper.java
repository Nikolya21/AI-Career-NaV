package org.example.aicareernav1.mapper;

import org.example.aicareernav1.dto.roadmap.*;
import org.example.aicareernav1.dto.roadmap.checkpoint.CheckpointSkeletonDTO;
import org.example.aicareernav1.dto.roadmap.response.LessonResponse;
import org.example.aicareernav1.dto.roadmap.response.TheoryResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.mapstruct.*;

import java.util.ArrayList;

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

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "checkpoint", ignore = true)
  @Mapping(target = "lessons", ignore = true) // Будем заполнять вручную через скелет
  Module toEntity(CheckpointSkeletonDTO dto);

  LessonResponse toResponse(Lesson lesson);

  TheoryResponse toTheoryResponse(Theory theory);

  // Твой вспомогательный метод для создания скелета (использовали в CheckpointService)
  /**
   * Используется в CheckpointService.deepenTopic для создания первичного скелета.
   * Мы игнорируем ID, берем userRequest как заголовок и привязываем к модулю.
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "title", source = "userRequest")
  @Mapping(target = "module", source = "module")
  @Mapping(target = "theory", ignore = true)
  @Mapping(target = "tasks", expression = "java(new java.util.ArrayList<>())")
  Lesson toSkeletonLesson(String userRequest, Module module);


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

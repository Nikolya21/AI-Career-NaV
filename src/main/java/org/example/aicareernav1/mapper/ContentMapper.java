package org.example.aicareernav1.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aicareernav1.dto.roadmap.*;
import org.example.aicareernav1.dto.roadmap.checkpoint.CheckpointSkeletonDTO;
import org.example.aicareernav1.dto.roadmap.response.LessonResponse;
import org.example.aicareernav1.dto.roadmap.response.TaskResponse;
import org.example.aicareernav1.dto.roadmap.response.TheoryResponse;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ContentMapper {

  @Autowired
  protected ObjectMapper objectMapper;

  // --- Entity Mappings ---

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "checkpoint", ignore = true)
  public abstract Module toEntity(ModuleDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "module", ignore = true)
  @Mapping(target = "theory", source = "theory")
  public abstract Lesson toEntity(LessonDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "lesson", ignore = true)
  @Mapping(target = "text", source = "text") // Урегулирование: DTO(content) -> Entity(text)
  public abstract Theory toEntity(TheoryDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "theory", ignore = true)
  public abstract Resource toEntity(ResourceDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "lesson", ignore = true)
  @Mapping(target = "content", expression = "java(dto.getContent().toString())")
  public abstract Task toEntity(TaskDTO dto);

  // ВЕРНУЛИ: Маппинг скелета
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "checkpoint", ignore = true)
  @Mapping(target = "lessons", ignore = true)
  public abstract Module toEntity(CheckpointSkeletonDTO dto);

  // --- Response Mappings ---

  public abstract LessonResponse toResponse(Lesson lesson);

  @Mapping(target = "text", source = "text") // Урегулирование: Entity(text) -> Response(text)
  public abstract TheoryResponse toTheoryResponse(Theory theory);

  @Mapping(target = "content", source = "content")
  public abstract TaskResponse toTaskResponse(Task task);

  // --- Special Methods ---

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "title", source = "userRequest")
  @Mapping(target = "module", source = "module")
  @Mapping(target = "theory", ignore = true)
  @Mapping(target = "tasks", expression = "java(new java.util.ArrayList<>())")
  public abstract Lesson toSkeletonLesson(String userRequest, Module module);

  /**
   * Конвертация String (DB) -> JsonNode (DTO) для задач
   */
  protected JsonNode mapStringToJsonNode(String content) {
    try {
      return (content == null || content.isEmpty())
              ? objectMapper.createObjectNode()
              : objectMapper.readTree(content);
    } catch (JsonProcessingException e) {
      return objectMapper.createObjectNode();
    }
  }

  @AfterMapping
  protected void linkRelations(@MappingTarget Module module) {
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
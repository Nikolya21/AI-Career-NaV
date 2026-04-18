package org.example.aicareernav1.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aicareernav1.dto.roadmap.checkpoint.CheckpointDTO;
import org.example.aicareernav1.dto.roadmap.checkpoint.DeepenCheckpointDTO;
import org.example.aicareernav1.dto.roadmap.TopicDTO;
import org.example.aicareernav1.dto.roadmap.response.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoadmapMapper {

  ModuleResponse toModuleResponse(Module module);

  LessonResponse toLessonResponse(Lesson lesson);

  TheoryResponse toTheoryResponse(Theory theory);

  @Mapping(target = "roadmapId", source = "topic.roadmap.id")
  CheckpointResponse toCheckpointResponse(Checkpoint checkpoint);

  @Mapping(target = "type", expression = "java(resource.getType().name())")
  ResourceResponse toResourceResponse(Resource resource);

  @Mapping(target = "type", expression = "java(task.getType().name())")
  @Mapping(target = "content", source = "content", qualifiedByName = "stringToJsonNode")
  TaskResponse toTaskResponse(Task task);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "roadmap", ignore = true)
  @Mapping(target = "checkpoints", source = "checkpoints")
  Topic toEntity(TopicDTO dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "topic", ignore = true)
  @Mapping(target = "status", constant = "LOCKED") // По умолчанию все закрыты
  Checkpoint toEntity(CheckpointDTO dto);

  // Маппинг из DTO глубокого изучения (от ИИ) в сущность
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "topic", ignore = true)
  @Mapping(target = "module", ignore = true)
  @Mapping(target = "status", constant = "ACTIVE")
  @Mapping(target = "title", source = "title")
  @Mapping(target = "description", source = "description") // Явное указание
  Checkpoint toEntity(DeepenCheckpointDTO dto);

  @AfterMapping
  default void linkCheckpointToTopic(@MappingTarget Topic topic) {
    if (topic.getCheckpoints() != null) {
      topic.getCheckpoints().forEach(cp -> cp.setTopic(topic));
    }
  }

  @Named("stringToJsonNode")
  default JsonNode stringToJsonNode(String content) {
    try {
      return new ObjectMapper().readTree(content);
    } catch (Exception e) {
      return new ObjectMapper().createObjectNode();
    }
  }
}
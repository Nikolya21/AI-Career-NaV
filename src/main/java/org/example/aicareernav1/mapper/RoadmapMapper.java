package org.example.aicareernav1.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.aicareernav1.dto.roadmap.LessonDTO;
import org.example.aicareernav1.dto.roadmap.ModuleDTO;
import org.example.aicareernav1.dto.roadmap.checkpoint.CheckpointDTO;
import org.example.aicareernav1.dto.roadmap.checkpoint.DeepenCheckpointDTO;
import org.example.aicareernav1.dto.roadmap.TopicDTO;
import org.example.aicareernav1.dto.roadmap.config.RoadmapConfigDTO;
import org.example.aicareernav1.dto.roadmap.response.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Module;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.example.aicareernav1.enums.CheckpointType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoadmapMapper {

  ModuleResponse toModuleResponse(Module module);

  LessonResponse toLessonResponse(Lesson lesson);

  TheoryResponse toTheoryResponse(Theory theory);

  @Mapping(target = "type", expression = "java(resource.getType().name())")
  ResourceResponse toResourceResponse(Resource resource);

  @Mapping(target = "type", expression = "java(task.getType().name())")
  @Mapping(target = "content", source = "content", qualifiedByName = "stringToJsonNode")
  TaskResponse toTaskResponse(Task task);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "roadmap", ignore = true)
  @Mapping(target = "checkpoints", source = "checkpoints")
  @Mapping(target = "title", source = "topicTitle")
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

  @Mapping(target = "progress", expression = "java(calculateProgress(roadmap))")
  @Mapping(target = "totalCheckpoints", expression = "java(calculateTotalCheckpoints(roadmap))")
  RoadmapResponse toResponse(Roadmap roadmap);

  // Самый важный маппинг: Чекпоинт и его связи
  @Mapping(target = "roadmapId", source = "roadmap.id")
  @Mapping(target = "parentCheckpointId", source = "parentCheckpoint.id")
  @Mapping(target = "sourceLessonId", source = "sourceLesson.id")
  CheckpointResponse toCheckpointResponse(Checkpoint checkpoint);


  /**
   * Считает все чекпоинты во всех топиках, исключая ROOT.
   */
  default Integer calculateTotalCheckpoints(Roadmap roadmap) {
    if (roadmap.getTopics() == null) return 0;

    return (int) roadmap.getTopics().stream()
            .flatMap(topic -> topic.getCheckpoints().stream())
            .filter(cp -> cp.getType() != CheckpointType.ROOT) // ROOT не считаем за учебный этап
            .count();
  }

  /**
   * Считает прогресс как % выполненных чекпоинтов (кроме ROOT).
   */
  default Double calculateProgress(Roadmap roadmap) {
    if (roadmap.getTopics() == null) return 0.0;

    List<Checkpoint> allSteps = roadmap.getTopics().stream()
            .flatMap(t -> t.getCheckpoints().stream())
            .filter(cp -> cp.getType() != CheckpointType.ROOT)
            .toList();

    if (allSteps.isEmpty()) return 0.0;

    long completed = allSteps.stream()
            .filter(cp -> cp.getStatus() == CheckpointStatus.COMPLETED)
            .count();

    return (double) completed / allSteps.size() * 100;
  }

  // Маппинг конфигурации
  RoadmapConfigDTO toConfigDto(RoadmapConfig config);

  // Маппинг топика (он подтянет CheckpointResponse автоматически,
  // если у тебя есть CheckpointMapper или метод ниже)
  TopicResponse toTopicResponse(Topic topic);


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
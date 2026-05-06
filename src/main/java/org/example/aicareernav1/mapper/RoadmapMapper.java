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
  @Mapping(target = "totalLessons", ignore = true)
  @Mapping(target = "completedLessons", ignore = true)
  CheckpointResponse toCheckpointResponse(Checkpoint checkpoint);

  // 3. Тот самый метод диагностики/наполнения
  @AfterMapping
  // Используем CheckpointResponse.CheckpointResponseBuilder[cite: 10, 12]
  default void fillProgress(Checkpoint checkpoint, @MappingTarget CheckpointResponse.CheckpointResponseBuilder response) {
    if (checkpoint.getModule() != null && checkpoint.getModule().getLessons() != null) {
      List<Lesson> lessons = checkpoint.getModule().getLessons();

      Integer total = lessons.size();
      Integer completed = (int) lessons.stream()
              .filter(l -> l.getTheory() != null &&
                      l.getTheory().getText() != null &&
                      !l.getTheory().getText().isBlank())
              .count();

      response.totalLessons(total);
      response.completedLessons(completed);
    } else {
      response.totalLessons(0);
      response.completedLessons(0);
    }
  }



  /**
   * Считает все чекпоинты во всех топиках, исключая ROOT.
   */
  default Integer calculateTotalCheckpoints(Roadmap roadmap) {
    if (roadmap.getTopics() == null) return 0;

    return (int) roadmap.getTopics().stream()
            .flatMap(t -> t.getCheckpoints().stream())
            .filter(cp -> cp.getType() != CheckpointType.ROOT)
            .count();
  }

  /**
   * Считает прогресс как % выполненных чекпоинтов (кроме ROOT).
   */
  default Double calculateProgress(Roadmap roadmap) {
    if (roadmap.getTopics() == null || roadmap.getTopics().isEmpty()) {
      return 0.0;
    }

    // 1. Собираем все чекпоинты (кроме ROOT)
    List<Checkpoint> allCheckpoints = roadmap.getTopics().stream()
            .flatMap(t -> t.getCheckpoints().stream())
            .filter(cp -> cp.getType() != CheckpointType.ROOT)
            .toList();

    int totalLessons = 0;
    int completedLessons = 0;

    // 2. Считаем уроки во всех чекпоинтах
    for (Checkpoint cp : allCheckpoints) {
      if (cp.getModule() != null && cp.getModule().getLessons() != null) {
        List<Lesson> lessons = cp.getModule().getLessons();
        totalLessons += lessons.size();

        completedLessons += (int) lessons.stream()
                .filter(l -> l.getTheory() != null &&
                        l.getTheory().getText() != null &&
                        !l.getTheory().getText().isBlank())
                .count();
      } else {
        totalLessons += 5;
      }
    }

    // 3. Защита от деления на ноль
    if (totalLessons == 0) {
      return 0.0;
    }

    // 4. Считаем процент
    double progress = (double) completedLessons / totalLessons * 100;

    // Округлим до 1 знака после запятой для красоты
    return Math.round(progress * 10.0) / 10.0;
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
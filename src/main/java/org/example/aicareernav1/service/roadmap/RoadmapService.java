package org.example.aicareernav1.service.roadmap;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.*;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.*;
import org.example.aicareernav1.enums.CheckpointStatus;
import org.example.aicareernav1.repository.roadmap.CheckpointRepository;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.repository.roadmap.TopicRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.roadmap.prompt.Prompts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления жизненным циклом дорожной карты обучения (Roadmap).
 * Обеспечивает генерацию структуры плана, наполнение этапов контентом через ИИ,
 * управление прогрессом пользователя и адаптивное расширение тем.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapService {

  private final RoadmapRepository roadmapRepository;
  private final TopicRepository topicRepository;
  private final CheckpointRepository checkpointRepository;
  private final GigaChatService gigaChatService;
  private final ObjectMapper objectMapper;

  /**
   * Создает базовую структуру (скелет) Roadmap на основе контекста пользователя.
   * Генерирует список блоков (Topic) и тем (Checkpoint), сохраняя их в БД.
   * После создания автоматически активирует самый первый этап.
   *
   * @param roadmapId   идентификатор созданной заготовки Roadmap
   * @param userContext описание целей пользователя или текст вакансии
   */
  @Transactional
  public void createSkeleton(Long roadmapId, String userContext) {
    Roadmap roadmap = roadmapRepository.findById(roadmapId)
      .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

    String systemPrompt = String.format(Prompts.SKELETON_SYSTEM_PROMPT, roadmap.getTargetJobTitle());
    String jsonResponse = fetchValidJson(userContext, systemPrompt, 2);

    try {
      SkeletonResponse response = objectMapper.readValue(jsonResponse, SkeletonResponse.class);
      int topicOrder = 0;
      for (TopicDTO tDto : response.getTopics()) {
        Topic topic = topicRepository.save(Topic.builder()
          .title(tDto.getTopicTitle())
          .orderIndex(topicOrder++)
          .roadmap(roadmap)
          .build());

        int cpOrder = 0;
        for (CheckpointDTO cpDto : tDto.getCheckpoints()) {
          checkpointRepository.save(Checkpoint.builder()
            .title(cpDto.getTitle())
            .description(cpDto.getDescription())
            .status(CheckpointStatus.LOCKED)
            .orderIndex(cpOrder++)
            .topic(topic)
            .build());
        }
      }
      activateFirstCheckpoint(roadmapId);
    } catch (Exception e) {
      log.error("Ошибка при создании структуры Roadmap: {}", e.getMessage());
    }
  }

  /**
   * Генерирует обучающий контент (уроки и задачи) для конкретной темы.
   * Использует ИИ для создания теории и практических упражнений в зависимости от профессии.
   *
   * @param checkpointId идентификатор этапа, который нужно наполнить
   */
  @Transactional
  public void fillCheckpointContent(Long checkpointId) {
    Checkpoint cp = checkpointRepository.findById(checkpointId)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));

    if (!cp.getLessons().isEmpty()) return;

    String profession = cp.getTopic().getRoadmap().getTargetJobTitle();
    String systemPrompt = String.format(Prompts.CONTENT_SYSTEM_PROMPT, profession, cp.getTitle());
    String json = fetchValidJson(cp.getDescription(), systemPrompt, 3);

    try {
      ContentResponse response = objectMapper.readValue(json, ContentResponse.class);
      for (LessonDTO lDto : response.getLessons()) {
        Lesson lesson = new Lesson();
        lesson.setTitle(lDto.getTitle());
        lesson.setCheckpoint(cp);

        for (TaskDTO tDto : lDto.getTasks()) {
          Task task = new Task();
          task.setTitle(tDto.getTitle());
          task.setType(tDto.getType());
          // Сохраняем вложенный JSON контент задачи как строку
          task.setContent(objectMapper.writeValueAsString(tDto.getContent()));
          task.setLesson(lesson);
          lesson.getTasks().add(task);
        }
        cp.getLessons().add(lesson);
      }
      checkpointRepository.save(cp);
      log.info("Контент для этапа '{}' успешно сгенерирован", cp.getTitle());
    } catch (Exception e) {
      log.error("Ошибка генерации контента для этапа {}: {}", checkpointId, e.getMessage());
    }
  }

  /**
   * Реализует фичу "Углубиться в тему".
   * Создает новый активный этап сразу после текущего, сдвигая порядок последующих тем.
   * Генерирует контент для нового этапа на основе уточняющего запроса пользователя.
   *
   * @param currentCheckpointId ID этапа, после которого нужно добавить углубление
   * @param userRequest          конкретный вопрос или область, которую пользователь хочет изучить подробнее
   * @return созданный и наполненный контентом новый Checkpoint
   */
  @Transactional
  public Checkpoint deepenTopic(Long currentCheckpointId, String userRequest) {
    Checkpoint current = checkpointRepository.findById(currentCheckpointId)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));

    // 1. Сдвигаем индексы последующих элементов в текущем топике
    List<Checkpoint> followers = checkpointRepository.findAllByTopicIdOrderByOrderIndexAsc(current.getTopic().getId());
    followers.stream()
      .filter(cp -> cp.getOrderIndex() > current.getOrderIndex())
      .forEach(cp -> {
        cp.setOrderIndex(cp.getOrderIndex() + 1);
        checkpointRepository.save(cp);
      });

    // 2. Генерация нового "глубокого" этапа через ИИ
    String json = fetchValidJson(userRequest, Prompts.DEEPEN_TOPIC_SYSTEM_PROMPT, 2);
    try {
      DeepenCheckpointDTO dto = objectMapper.readValue(json, DeepenCheckpointDTO.class);
      Checkpoint deep = checkpointRepository.save(Checkpoint.builder()
        .title(dto.getTitle())
        .description(dto.getDescription())
        .status(CheckpointStatus.ACTIVE)
        .topic(current.getTopic())
        .orderIndex(current.getOrderIndex() + 1)
        .parentCheckpointId(current.getId())
        .build());

      // Помечаем текущий как завершенный, так как пользователь перешел к углублению
      current.setStatus(CheckpointStatus.COMPLETED);
      checkpointRepository.save(current);

      // 3. Сразу генерируем уроки для нового этапа
      fillCheckpointContent(deep.getId());
      return deep;
    } catch (Exception e) {
      log.error("Ошибка при создании углубленного этапа: {}", e.getMessage());
      throw new RuntimeException("Не удалось создать углубление темы");
    }
  }

  /**
   * Вспомогательный метод для получения гарантированно валидного JSON от ИИ.
   * В случае ошибки парсинга выполняет повторные запросы (Retry) с указанием на ошибку.
   *
   * @param context      входные данные (сообщение пользователя)
   * @param systemPrompt инструкция для ИИ
   * @param attempts     количество попыток
   * @return строка в формате JSON
   */
  private String fetchValidJson(String context, String systemPrompt, int attempts) {
    String lastResponse = "";
    for (int i = 0; i < attempts; i++) {
      lastResponse = gigaChatService.chat(systemPrompt, context);
      if (isValidJson(lastResponse)) return lastResponse;
      log.warn("Попытка {}: ИИ выдал некорректный JSON. Повтор...", i + 1);
    }
    return "{}";
  }

  /**
   * Проверяет, является ли строка валидным JSON-объектом.
   */
  private boolean isValidJson(String json) {
    try {
      objectMapper.readTree(json);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Находит самый первый этап в самой первой теме Roadmap и делает его доступным для изучения.
   *
   * @param roadmapId ID дорожной карты
   */
  private void activateFirstCheckpoint(Long roadmapId) {
    topicRepository.findAllByRoadmapIdOrderByOrderIndexAsc(roadmapId).stream()
      .findFirst().ifPresent(t -> {
        checkpointRepository.findAllByTopicIdOrderByOrderIndexAsc(t.getId()).stream()
          .findFirst().ifPresent(cp -> {
            cp.setStatus(CheckpointStatus.ACTIVE);
            checkpointRepository.save(cp);
            fillCheckpointContent(cp.getId());
          });
      });
  }
}
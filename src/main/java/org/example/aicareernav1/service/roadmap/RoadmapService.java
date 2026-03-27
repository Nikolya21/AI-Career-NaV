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
    String jsonResponse = cleanJsonResponse(fetchValidJson(userContext, systemPrompt, 2));


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
   * Анализирует отзыв пользователя о прошедшем этапе и обновляет профиль обучения.
   * @param roadmapId ID дорожной карты
   * @param feedbackText текст отзыва от пользователя
   */
  @Transactional
  public void processUserFeedback(Long roadmapId, String feedbackText) {
    Roadmap roadmap = roadmapRepository.findById(roadmapId)
      .orElseThrow(() -> new EntityNotFoundException("Roadmap not found"));

    String currentNotes = roadmap.getLearningStyleNotes() != null
                          ? roadmap.getLearningStyleNotes()
                          : "Информации пока нет.";

    // Промпт, который заставляет ИИ кратко обновить портрет ученика
    String systemPrompt = "Ты — аналитик образовательного процесса. Твоя задача — обновить профиль предпочтений студента.\n" +
      "Текущий профиль: " + currentNotes + "\n" +
      "Новый отзыв: " + feedbackText + "\n" +
      "Выдай обновленный список предпочтений кратко, через запятую. Сосредоточься на стиле подачи, сложности и формате заданий.";

    String updatedNotes = gigaChatService.chat(systemPrompt, "Обнови профиль на основе отзыва.");

    roadmap.setLearningStyleNotes(updatedNotes);
    roadmapRepository.save(roadmap);

    log.info("Профиль обучения обновлен для Roadmap ID {}: {}", roadmapId, updatedNotes);
  }

  /**
   * Генерирует обучающий контент (уроки и задачи) для конкретной темы.
   * Использует ИИ для создания теории и практических упражнений в зависимости от профессии.
   *
   * @param checkpointId идентификатор этапа, который нужно наполнить
   */
  @Transactional
  public void fillCheckpointContent(Long checkpointId) {
    log.info("==> [GENERATE CONTENT] Старт генерации для Checkpoint ID: {}", checkpointId);

    Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
      .orElseThrow(() -> {
        log.error("Checkpoint ID {} не найден в базе данных", checkpointId);
        return new EntityNotFoundException("Checkpoint не найден");
      });

    if (!checkpoint.getLessons().isEmpty()) {
      log.info("Контент для чекпоинта '{}' уже существует. Генерация пропущена.", checkpoint.getTitle());
      return;
    }

    Roadmap roadmap = checkpoint.getTopic().getRoadmap();
    String profession = roadmap.getTargetJobTitle();
    String style = (roadmap.getLearningStyleNotes() != null)
      ? roadmap.getLearningStyleNotes()
      : "Стандартный подход";

    // 1. Статичные правила
    String systemPrompt = Prompts.CONTENT_SYSTEM_PROMPT;

    // 2. Динамическое задание
    String userMsg = String.format(
      "Создай уроки для специалиста в области: %s.\n" +
        "Тема: %s.\n" +
        "Контекст: %s.\n" +
        "Стиль обучения пользователя: %s",
      profession, checkpoint.getTitle(), checkpoint.getDescription(), style
    );

    log.debug("Отправляемый запрос к ИИ (User Message):\n{}", userMsg);

    // Вызов ИИ через механизм Retry
    String json = fetchValidJson(userMsg, systemPrompt, 3);

    try {
      log.info("Получен ответ от ИИ. Начинаю парсинг JSON для этапа: {}", checkpoint.getTitle());
      // Дополнительная предосторожность перед парсингом
      String sanitizedJson = cleanJsonResponse(json);
      log.info("Очищенный JSON для десериализации: {}", sanitizedJson);

      ContentResponse response = objectMapper.readValue(sanitizedJson, ContentResponse.class);

      if (response == null || response.getLessons() == null || response.getLessons().isEmpty()) {
        log.error("ИИ вернул пустой список уроков или null для Checkpoint {}", checkpointId);
        return;
      }

      log.info("ИИ сгенерировал {} уроков.", response.getLessons().size());

      for (LessonDTO lDto : response.getLessons()) {
        Lesson lesson = new Lesson();
        lesson.setTitle(lDto.getTitle());
        lesson.setCheckpoint(checkpoint);

        log.debug("Обработка урока: {}. Количество задач: {}", lDto.getTitle(), lDto.getTasks().size());

        for (TaskDTO tDto : lDto.getTasks()) {
          Task task = new Task();
          task.setTitle(tDto.getTitle());
          task.setType(tDto.getType());
          // Сохраняем вложенный JSON контент задачи как строку
          task.setContent(objectMapper.writeValueAsString(tDto.getContent()));
          task.setLesson(lesson);
          lesson.getTasks().add(task);
        }
        checkpoint.getLessons().add(lesson);
      }

      checkpointRepository.save(checkpoint);
      log.info("<== [SUCCESS] Контент для этапа '{}' успешно сохранен в БД", checkpoint.getTitle());

    } catch (Exception e) {
      log.error("!!! [ERROR] Ошибка при обработке ответа ИИ для этапа {}. \n" +
          "Ошибка: {} \n" +
          "Сырой JSON ответа: {}",
        checkpointId, e.getMessage(), json);
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
  public Checkpoint deepenTopic(Long currentCheckpointId, String userRequest) { //todo: выдает ошибку - разобраться
    log.info("Запрос на углубление темы после Checkpoint ID: {}. Запрос: '{}'", currentCheckpointId, userRequest);
    Checkpoint current = checkpointRepository.findById(currentCheckpointId)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));

    List<Checkpoint> followers = checkpointRepository.findAllByTopicIdOrderByOrderIndexAsc(current.getTopic().getId());
    log.info("Сдвиг индексов для {} последующих чекпоинтов", followers.size());

    followers.stream()
      .filter(cp -> cp.getOrderIndex() > current.getOrderIndex())
      .forEach(cp -> {
        int oldIdx = cp.getOrderIndex();
        cp.setOrderIndex(oldIdx + 1);
        log.debug("Checkpoint '{}': index {} -> {}", cp.getTitle(), oldIdx, cp.getOrderIndex());
        checkpointRepository.save(cp);
      });

    // 2. Генерация нового "глубокого" этапа через ИИ
    String json = cleanJsonResponse(fetchValidJson(userRequest, Prompts.DEEPEN_TOPIC_SYSTEM_PROMPT, 2));
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
      log.info("Запрос к ИИ (Попытка {}/{})", i + 1, attempts);
      lastResponse = gigaChatService.chat(systemPrompt, context);

      // КРИТИЧЕСКОЕ ИЗМЕНЕНИЕ: Очищаем ДО проверки на валидность
      String cleaned = cleanJsonResponse(lastResponse);

      if (isValidJson(cleaned)) {
        log.info("Получен валидный JSON от ИИ после очистки.");
        return cleaned; // Возвращаем уже чистый JSON
      }

      log.warn("Попытка {}: ИИ выдал невалидный JSON. Некорректный текст: {}", i + 1, lastResponse);

      // Улучшаем подсказку для ИИ, требуя "голый" JSON
      context = "Твой предыдущий ответ был невалидным JSON. Исправь его. " +
        "ВАЖНО: Выведи только чистый JSON-код без слов ```json или любого другого текста.\n" +
        "Предыдущий ответ: " + lastResponse;
    }
    log.error("Все {} попыток получить JSON провалены.", attempts);
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

  private String cleanJsonResponse(String rawResponse) {
    if (rawResponse == null || rawResponse.isBlank()) return "{}";

    // 1. Пытаемся найти границы JSON объекта, если ИИ добавил лишний текст
    try {
      int firstBrace = rawResponse.indexOf('{');
      int lastBrace = rawResponse.lastIndexOf('}');

      if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
        String jsonPart = rawResponse.substring(firstBrace, lastBrace + 1);

        // 2. Убираем остатки Markdown-разметки внутри найденного куска
        return jsonPart
          .replace("```json", "")
          .replace("```", "")
          .trim();
      }
    } catch (Exception e) {
      log.warn("Не удалось очистить JSON регуляркой: {}", e.getMessage());
    }

    return rawResponse.trim();
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

  @Transactional(readOnly = true)
  public Roadmap getRoadmapWithTopics(Long id) {
    log.info("Получение полной структуры Roadmap ID: {}", id);
    Roadmap roadmap = roadmapRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

    // Принудительная инициализация ленивых коллекций,
    // чтобы они были доступны в контроллере/шаблоне
    roadmap.getTopics().size();
    roadmap.getTopics().forEach(t -> t.getCheckpoints().size());

    return roadmap;
  }


  @Transactional(readOnly = true)
  public double calculateProgress(Long roadmapId) {
    Roadmap roadmap = roadmapRepository.findById(roadmapId)
      .orElseThrow(() -> new EntityNotFoundException("Roadmap не найден"));

    List<Checkpoint> allCheckpoints = roadmap.getTopics().stream()
      .flatMap(t -> t.getCheckpoints().stream())
      .toList();

    if (allCheckpoints.isEmpty()) return 0.0;

    long completedCount = allCheckpoints.stream()
      .filter(cp -> cp.getStatus() == CheckpointStatus.COMPLETED)
      .count();

    return (double) completedCount / allCheckpoints.size() * 100;
  }

  @Transactional(readOnly = true)
  public Checkpoint getCheckpoint(Long id) {
    return checkpointRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Checkpoint не найден"));
  }
}
package org.example.aicareernav1.controller.roadmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.RoadmapGenerationRequest;
import org.example.aicareernav1.dto.roadmap.response.CheckpointResponse;
import org.example.aicareernav1.dto.roadmap.response.LessonResponse;
import org.example.aicareernav1.dto.roadmap.response.RoadmapResponse;
import org.example.aicareernav1.service.roadmap.LessonService;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления образовательным процессом дорожной карты.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

  private final RoadmapService roadmapService;
  private final LessonService lessonService;

  /**
   * Центральная ROOT вершина redirect to...
   * Возвращает redirect.
   */
  //todo: redirect должен происходить по userId => по RoadmapId нужно искать UserId, но... пока такого функционала нет:) (Георгий блядотович спасибо!)
  @GetMapping("/{roadmapId}/root-action")
  public ResponseEntity<Map<String, String>> getRootRedirect(@PathVariable Long roadmapId) {
    // Ваша логика: куда именно должен попасть пользователь при клике на ROOT
    String targetUrl = "/dashboard/roadmap/" + roadmapId + "/statistics";
    Map<String, String> response = new HashMap<>();
    response.put("redirectUrl", targetUrl);

    return ResponseEntity.ok(response);
  }

  /**
   * Полная генерация дорожной карты: создание сущности + генерация структуры через ИИ.
   * Возвращает ID созданного Roadmap для последующего редиректа.
   */
  @PostMapping("/generate")
  public ResponseEntity<RoadmapResponse> generateRoadmap(@RequestBody RoadmapGenerationRequest request) {
    log.info("API: Запрос на полную генерацию Roadmap для вакансии: {}", request.getJobTitle());
    RoadmapResponse roadmap = roadmapService.generateFullRoadmap(request);
    roadmapService.processUserFeedback(roadmap.getId(), request.getTestResult());
    return ResponseEntity.ok(roadmap);
  }

  /**
   * Точка входа в чекпоинт.
   * Если пользователь заходит впервые — этап активируется и нарезаются уроки.
   * Если заходит повторно — просто возвращается структура этапа.
   */
  @GetMapping("/checkpoint/{id}")
  public ResponseEntity<CheckpointResponse> enterCheckpoint(@PathVariable Long id) {
    log.info("API: Вход в чекпоинт ID: {}", id);
    return ResponseEntity.ok(roadmapService.getOrStartCheckpoint(id));
  }

  /**
   * Отправляет отзыв пользователя о пройденном этапе для адаптации будущих модулей.
   *
   * @param id       идентификатор Roadmap
   * @param feedback текст отзыва
   */
  @PostMapping("/{id}/feedback")
  public ResponseEntity<Void> sendFeedback(@PathVariable Long id, @RequestBody String feedback) {
    log.info("API: Получен фидбек для Roadmap ID: {}", id);
    roadmapService.processUserFeedback(id, feedback);
    return ResponseEntity.accepted().build();
  }

  /**
   * Создает новый промежуточный чекпоинт для более глубокого изучения текущей темы.
   * Возвращает CheckpointResponse вместо прямой Entity для безопасности данных.
   *
   * @param id      идентификатор текущего чекпоинта
   * @param request уточняющий запрос пользователя (что именно изучить подробнее)
   * @return DTO созданного чекпоинта
   */
  @PostMapping("/lesson/{id}/deepen")
  //@PostMapping("/checkpoint/{id}/deepen")
  public ResponseEntity<CheckpointResponse> deepenTopic(@PathVariable Long id, @RequestBody String request) {
    log.info("API: Запрос на углубление темы для Checkpoint ID: {}", id);
    CheckpointResponse response = roadmapService.deepenTopicProcess(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Возвращает текущий прогресс прохождения дорожной карты в процентах.
   *
   * @param id ID дорожной карты
   * @return процент завершения (0.0 - 100.0)
   */
  @GetMapping("/{id}/progress")
  public ResponseEntity<Double> getProgress(@PathVariable Long id) {
    return ResponseEntity.ok(roadmapService.calculateProgress(id));
  }

  /**
   * Генерирует или возвращает контент конкретного урока.
   * Именно этот метод должен вызываться, когда пользователь нажимает на урок в интерфейсе (Сценарий с MAIN Checkpoint).
   */
  @GetMapping("/lesson/{lessonId}")
  public ResponseEntity<LessonResponse> getLessonContent(@PathVariable Long lessonId) {
    log.info("API: Запрос контента для конкретного урока ID: {}", lessonId);
    return ResponseEntity.ok(lessonService.getAndFillLesson(lessonId));
  }

  /**
   * Возвращает полные данные для отрисовки графа (все узлы и связи).
   */
  @GetMapping("/{id}/graph-data")
  public ResponseEntity<RoadmapResponse> getGraphData(@PathVariable Long id) {
    // В сервисе нужно реализовать получение Roadmap со ВСЕМИ вложенными чекпоинтами
    // (включая дочерние для DEEPEN чекпоинтов)
    RoadmapResponse response = roadmapService.getFullGraphResponse(id);
    return ResponseEntity.ok(response);
  }
}
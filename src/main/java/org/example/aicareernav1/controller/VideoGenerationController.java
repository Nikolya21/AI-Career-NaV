package org.example.aicareernav1.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.VideoTaskDTO;
import org.example.aicareernav1.service.VideoGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


/**
 * REST-контроллер для управления процессами генерации видео-анимаций.
 * Обеспечивает интерфейс для запуска новых задач и отслеживания их состояния.
 * Взаимодействует с внешним сервисом рендеринга на базе Manim через асинхронный мост.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoGenerationController {

  private final VideoGenerationService videoGenerationService;

  /**
   * Инициирует процесс создания новой видео-анимации по заданной теме.
   * Запрос выполняется асинхронно: клиент получает подтверждение с ID задачи,
   * в то время как рендеринг продолжается в фоновом режиме на Python-сервисе.
   *
   * @param topic Тема или концепция, которую необходимо визуализировать.
   * @return Mono, содержащий ResponseEntity со статусом 202 (Accepted) и данными созданной задачи.
   */
  @PostMapping("/generate")
  public Mono<ResponseEntity<VideoTaskDTO>> generate(@RequestParam String topic) {
    log.info("Received request to generate video for topic: {}", topic);

    return videoGenerationService.createVideoTask(topic)
      .map(dto -> ResponseEntity.accepted().body(dto))
      .doOnError(e -> log.error("Error starting video generation: {}", e.getMessage()));
  }

  /**
   * Возвращает текущую информацию о задаче генерации из локальной базы данных.
   * Позволяет фронтенду опрашивать состояние (Polling) для обновления интерфейса.
   *
   * @param taskId Уникальный идентификатор задачи.
   * @return ResponseEntity с текущим статусом и (если готово) ссылкой на видеофайл.
   * @throws RuntimeException если задача с указанным ID не найдена в БД.
   */
  @GetMapping("/{taskId}/status")
  public ResponseEntity<VideoTaskDTO> getStatus(@PathVariable String taskId) {
    log.info("Checking status for task: {}", taskId);


    VideoTaskDTO statusDto = videoGenerationService.getVideoById(taskId);
    return ResponseEntity.ok(statusDto);
  }
}

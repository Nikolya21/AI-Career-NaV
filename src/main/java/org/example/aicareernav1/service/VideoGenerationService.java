package org.example.aicareernav1.service;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.VideoTaskDTO;
import org.example.aicareernav1.entity.VideoTask;
import org.example.aicareernav1.enums.VideoStatus;
import org.example.aicareernav1.mapper.VideoTaskMapper;
import org.example.aicareernav1.repository.VideoTaskRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Сервис для управления бизнес-логикой генерации видео.
 * Является связующим звеном между хранилищем данных (PostgreSQL) и
 * внешним API генерации (FastAPI + Manim).
 */
@Service
@RequiredArgsConstructor
public class VideoGenerationService {

  private final VideoTaskRepository repository;
  private final WebClient manimWebClient;
  private final VideoTaskMapper mapper;

  /**
   * Выполняет регистрацию новой задачи в системе и отправляет запрос на удаленный рендеринг.
   * Использует реактивный подход для предотвращения блокировки потоков при ожидании ответа от API.
   *
   * @param topic Строковое описание темы для анимации.
   * @return Mono с объектом VideoTaskDTO, содержащим присвоенный UUID.
   */
  public Mono<VideoTaskDTO> createVideoTask(String topic) {
    return manimWebClient.post()
      .uri(uri -> uri.path("/generate").queryParam("topic", topic).build())
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
      .map(response -> {
        String uuid = response.get("task_id");
        VideoTask task = VideoTask.builder()
          .taskId(uuid)
          .topic(topic)
          .status(VideoStatus.PENDING)
          .build();
        return repository.save(task);
      })
      .map(mapper::toDto);
  }

  /**
   * Поиск информации о задаче в базе данных по её идентификатору.
   *
   * @param Id Первичный ключ задачи в БД.
   * @return Данные задачи, преобразованные в DTO.
   * @throws RuntimeException Если запись отсутствует в репозитории.
   */
  public VideoTaskDTO getVideoById(String Id) {
    VideoTask videoTask = repository.findById(Id)
      .orElseThrow(() -> new RuntimeException("Видео с ID " + Id + " не найдено"));

    return mapper.toDto(videoTask);
  }
}
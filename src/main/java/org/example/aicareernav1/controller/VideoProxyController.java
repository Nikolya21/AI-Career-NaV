package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
public class VideoProxyController {

  private final WebClient pythonWebClient = WebClient.builder()
      .baseUrl("http://python-fastapi-service:5000")
      .build();

  @PostMapping("/process-frame")
  public Mono<ResponseEntity<Map>> processFrame(
      @RequestParam("image") MultipartFile image,
      @RequestParam("question_id") String questionId) {
    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("image", new ByteArrayResource(image.getBytes()) {
        @Override
        public String getFilename() {
          return "frame.jpg";
        }
      });
      body.add("question_id", questionId);

      return pythonWebClient.post()
          .uri("/process-frame")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(body)
          .retrieve()
          .bodyToMono(Map.class)
          .map(ResponseEntity::ok)
          .onErrorResume(e -> {
            log.error("Ошибка при вызове Python-сервера (process-frame)", e);
            return Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage())));
          });
    } catch (IOException e) {
      log.error("Ошибка чтения файла изображения", e);
      return Mono.just(ResponseEntity.status(500).body(Map.of("error", "Cannot read image file")));
    }
  }

  @GetMapping("/get-stats")
  public Mono<ResponseEntity<Map>> getStats(@RequestParam("question_id") String questionId) {
    return pythonWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/get-stats")
            .queryParam("question_id", questionId)
            .build())
        .retrieve()
        .bodyToMono(Map.class)
        .map(ResponseEntity::ok)
        .onErrorResume(e -> {
          log.error("Ошибка при вызове Python-сервера (get-stats)", e);
          return Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage())));
        });
  }
}
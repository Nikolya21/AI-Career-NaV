package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AudioProcessingController {

  private final WebClient webClient = WebClient.builder()
      .baseUrl("http://python-fastapi-service:5000")
      .build();

  @PostMapping("/api/process-audio")
  public Mono<ResponseEntity<Map>> processAudio(@RequestParam("audio") MultipartFile audioFile) {
    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("audio", new ByteArrayResource(audioFile.getBytes()) {
        @Override
        public String getFilename() {
          return "audio.webm";
        }
      });

      return webClient.post()
          .uri("/process-audio")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(body)
          .retrieve()
          .bodyToMono(Map.class)
          .map(result -> ResponseEntity.ok(result))
          .onErrorResume(e -> {
            log.error("Ошибка при вызове Python-сервиса", e);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage())));
          });
    } catch (IOException e) {
      return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Ошибка чтения файла: " + e.getMessage())));
    }
  }
}
package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
public class VideoProxyController {

  private final WebClient pythonWebClient = WebClient.builder()
      .baseUrl("http://localhost:5000")
      .build();

  @PostMapping("/api/proxy/detect-face-touches")
  public Mono<ResponseEntity<Map>> detectFaceTouches(@RequestParam("image") MultipartFile image) {
    try {
      // Подготавливаем multipart тело запроса
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("image", new ByteArrayResource(image.getBytes()) {
        @Override
        public String getFilename() {
          return "frame.jpg";
        }
      });

      return pythonWebClient.post()
          .uri("/detect-face-touches")
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .bodyValue(body)
          .retrieve()
          .bodyToMono(Map.class)
          .map(ResponseEntity::ok)
          .onErrorResume(e -> {
            log.error("Ошибка при вызове Python-сервера", e);
            return Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage())));
          });
    } catch (IOException e) {
      log.error("Ошибка чтения файла изображения", e);
      return Mono.just(ResponseEntity.status(500).body(Map.of("error", "Cannot read image file")));
    }
  }
}
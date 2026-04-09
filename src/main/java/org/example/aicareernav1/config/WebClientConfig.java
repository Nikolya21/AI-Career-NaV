package org.example.aicareernav1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
  @Bean
  public WebClient manimWebClient() {
    return WebClient.builder()
            .baseUrl("http://localhost:8000") // адрес python сервиса с manim генерацией анимации(видео)
            .build();
  }
}

package org.example.aicareernav1.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class HhOAuthService {

  @Value("${hh.oauth.client-id}")
  private String clientId;

  @Value("${hh.oauth.client-secret}")
  private String clientSecret;

  @Value("${hh.oauth.token-uri:https://hh.ru/oauth/token}")
  private String tokenUri;

  private String accessToken;
  private Instant expiresAt;

  private final WebClient webClient = WebClient.builder().build();

  public synchronized Mono<String> getAccessToken() {
    if (accessToken != null && expiresAt != null && Instant.now().isBefore(expiresAt)) {
      return Mono.just(accessToken);
    }
    return fetchNewToken();
  }

  private Mono<String> fetchNewToken() {
    log.info("🔄 Запрашиваем новый OAuth-токен у hh.ru...");

    String credentials = clientId + ":" + clientSecret;
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

    return webClient.post()
        .uri(tokenUri)
        .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
        .retrieve()
        .bodyToMono(Map.class)
        .map(response -> {
          accessToken = (String) response.get("access_token");
          Integer expiresIn = (Integer) response.get("expires_in");
          if (expiresIn != null) {
            expiresAt = Instant.now().plusSeconds(expiresIn - 60);
          }
          log.info("✅ Токен получен, истекает: {}", expiresAt);
          return accessToken;
        })
        .doOnError(e -> log.error("❌ Ошибка получения токена: {}", e.getMessage()));
  }
}
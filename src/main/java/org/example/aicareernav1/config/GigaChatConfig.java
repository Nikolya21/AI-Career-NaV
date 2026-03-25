package org.example.aicareernav1.config;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GigaChatConfig {

  @Value("${gigachat.client-id:}")
  private String clientId;

  @Value("${gigachat.secret:}")  // Изменено с client-secret на secret
  private String clientSecret;

  @Value("${gigachat.auth-key:}")  // Добавлена поддержка auth-key
  private String authKey;

  @Value("${gigachat.scope:GIGACHAT_API_PERS}")
  private String scope;

  @Value("${gigachat.read-timeout-ms:30000}")  // Исправлено название
  private int readTimeoutMs;

  @Value("${gigachat.verify-ssl-certs:false}")
  private boolean verifySslCerts;

  /**
   * Создаёт бин GigaChatClient для использования в сервисах
   */
  @Bean
  public GigaChatClient gigaChatClient() {
    log.info("Создание GigaChatClient с таймаутом {} мс", readTimeoutMs);

    // Определяем scope из строки
    Scope gigaScope;
    try {
      gigaScope = Scope.valueOf(scope);
    } catch (IllegalArgumentException e) {
      log.warn("Неизвестный scope: {}, используем GIGACHAT_API_PERS", scope);
      gigaScope = Scope.GIGACHAT_API_PERS;
    }

    // Определяем authKey
    String finalAuthKey;
    if (authKey != null && !authKey.isEmpty()) {
      // Используем готовый auth-key
      finalAuthKey = authKey;
      log.info("Используем готовый auth-key");
    } else if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
      // Формируем authKey из clientId и secret
      finalAuthKey = clientId + ":" + clientSecret;
      log.info("Используем client-id и secret для аутентификации");
    } else {
      log.error("Не настроены credentials для GigaChat! Укажите либо auth-key, либо client-id + secret");
      throw new IllegalStateException("GigaChat credentials not configured");
    }

    // Создаем OAuth клиент с правильным authKey
    AuthClient authClient = AuthClient.builder()
      .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
        .scope(gigaScope)
        .authKey(finalAuthKey)
        .build())
      .build();

    return GigaChatClient.builder()
      .verifySslCerts(verifySslCerts)
      .readTimeout(readTimeoutMs)
      .authClient(authClient)
      .build();
  }
}
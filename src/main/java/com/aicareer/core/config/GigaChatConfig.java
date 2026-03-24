package com.aicareer.core.config;

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

  @Value("${gigachat.client-secret:}")
  private String clientSecret;

  @Value("${gigachat.scope:GIGACHAT_API_PERS}")
  private String scope;

  @Value("${gigachat.timeout-seconds:30}")
  private int timeoutSeconds;

  @Value("${gigachat.verify-ssl-certs:false}")
  private boolean verifySslCerts;

  /**
   * Создаёт бин GigaChatClient для использования в сервисах
   */
  @Bean
  public GigaChatClient gigaChatClient() {
    log.info("Создание GigaChatClient с таймаутом {} секунд", timeoutSeconds);

    // Определяем scope из строки
    Scope gigaScope;
    try {
      gigaScope = Scope.valueOf(scope);
    } catch (IllegalArgumentException e) {
      log.warn("Неизвестный scope: {}, используем GIGACHAT_API_PERS", scope);
      gigaScope = Scope.GIGACHAT_API_PERS;
    }

    // Собираем authKey из clientId:clientSecret
    String authKey = clientId + ":" + clientSecret;

    return GigaChatClient.builder()
        .verifySslCerts(verifySslCerts)
        .readTimeout(timeoutSeconds * 1000)  // переводим в миллисекунды
        .authClient(AuthClient.builder()
            .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
                .scope(gigaScope)
                .authKey(authKey)
                .build())
            .build())
        .build();
  }
}
package org.example.aicareernav1.config;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GigaChatConfig {

  @Value("${gigachat.auth-key}")
  private String authKey;

  @Value("${gigachat.read-timeout-ms:30000}")
  private int readTimeout;

  @Value("${gigachat.verify-ssl-certs:false}")
  private boolean verifySslCerts;

  @Bean
  public GigaChatClient gigaChatClient() {
    return GigaChatClient.builder()
        .verifySslCerts(verifySslCerts)
        .readTimeout(readTimeout)
        .authClient(AuthClient.builder()
            .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
                .scope(Scope.GIGACHAT_API_PERS)
                .authKey(authKey)
                .build())
            .build())
        .build();
  }
}
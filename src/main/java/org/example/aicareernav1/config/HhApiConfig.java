package org.example.aicareernav1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HhApiConfig {

  @Value("${hh.api.base-url:https://api.hh.ru}")
  private String hhBaseUrl;

  @Bean
  public WebClient hhWebClient() {
    return WebClient.builder()
        .baseUrl(hhBaseUrl)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "UTF-8")
        .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
        .build();
  }
}
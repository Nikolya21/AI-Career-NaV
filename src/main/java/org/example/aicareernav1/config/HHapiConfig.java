package org.example.aicareernav1.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HHapiConfig {

  @Bean
  public WebClient hhWebClient(){
    return WebClient.builder()
        .baseUrl("https://api.hh.ru/?text=%s&area=%s&per_page=%d")
        .defaultHeader(HttpHeaders.USER_AGENT, "AiCareerNavigator qolchenkoalex@gmail.com")
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "UTF-8")
        .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
        .build();
  }
}

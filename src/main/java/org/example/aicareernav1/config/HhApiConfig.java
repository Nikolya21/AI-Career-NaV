package org.example.aicareernav1.config;

import org.example.aicareernav1.service.parser.HhOAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class HhApiConfig {

  @Value("${hh.api.base-url:https://api.hh.ru}")
  private String hhBaseUrl;

  private final HhOAuthService oauthService;

  public HhApiConfig(HhOAuthService oauthService) {
    this.oauthService = oauthService;
  }

  @Bean
  public WebClient hhWebClient() {
    return WebClient.builder()
        .baseUrl(hhBaseUrl)
        .filter(addBearerToken())  // добавляем фильтр для подстановки токена
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_CHARSET, "UTF-8")
        .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
        .build();
  }

  private ExchangeFilterFunction addBearerToken() {
    return (request, next) ->
        oauthService.getAccessToken()
            .flatMap(token -> {
              var newRequest = org.springframework.web.reactive.function.client.ClientRequest.from(request)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                  .build();
              return next.exchange(newRequest);
            });
  }
}
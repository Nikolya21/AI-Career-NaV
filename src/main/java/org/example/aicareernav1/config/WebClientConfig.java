package org.example.aicareernav1.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${python.service.url:http://127.0.0.1:8000}") // По умолчанию 127.0.0.1 для локального запуска
    private String pythonServiceUrl;

    @Bean
    public WebClient pythonServiceClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 секунд на коннект
                .responseTimeout(Duration.ofSeconds(30)) // Ждем ответ от RAG до 30 сек
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        return builder
                .baseUrl(pythonServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> {
                    System.out.println("Sending request: " + request.method() + " " + request.url());
                    return next.exchange(request);
                })
                .build();
    }
}

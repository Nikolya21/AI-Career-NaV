package org.example.aicareernav1.service.integration;

import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SaveRequest;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;

@Service
public class PythonIntegrationService {
    private final WebClient webClient;

    public PythonIntegrationService(@Qualifier("pythonServiceClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public GatewayResponse searchInRag(SearchRequest request) {
        return webClient.post()
                .uri("/api/v1/lessons/search") // Путь к эндпоинту в FastAPI
                .body(Mono.just(request), SearchRequest.class)
                .retrieve()
                .bodyToMono(GatewayResponse.class)
                .onErrorResume(e -> {
                    System.err.println("Ошибка RAG поиска: " + e.getMessage());
                    // Возвращаем пустой ответ с ошибкой, чтобы не ломать цепочку вызовов
                    return Mono.just(createErrorResponse("NOT_FOUND", "Python service unavailable"));
                })
                .block(); // Синхронное ожидание для простоты текущей реализации
    }


    public void saveProcessedContent(SaveRequest saveRequest) {
        webClient.post()
                .uri("/api/v1/lessons/save") // Проверь путь в FastAPI
                .bodyValue(saveRequest)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> System.err.println("Ошибка при сохранении в Python: " + e.getMessage()))
                .subscribe(); // Асинхронно, так как нам не обязательно ждать подтверждения для продолжения работы Java
    }

    private GatewayResponse createErrorResponse(String status, String message) {
        GatewayResponse response = new GatewayResponse();
        response.setStatus(status);
        response.setContent(message);
        response.setResources(new ArrayList<>());
        response.setChunks(new ArrayList<>());
        return response;
    }
}
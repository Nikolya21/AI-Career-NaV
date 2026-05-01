package parserTest;

import org.example.aicareernav1.dto.hhDto.HhVacanciesResponse;
import org.example.aicareernav1.dto.hhDto.HhVacancyItem;
import org.example.aicareernav1.model.vacancy.RealVacancy;
import org.example.aicareernav1.service.parser.ParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParserServiceTest {

    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    private ParserService parserService;

    @BeforeEach
    void setUp() {
        // WebClient требует длинной цепочки моков
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // Исправляем цепочку для onStatus (нужно замокнуть дважды для 4xx и 5xx)
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        parserService = new ParserService(webClient);
    }

    @Test
    void getVacancies_ShouldReturnMappedVacancies() {
        // Имитируем ответ от API
        HhVacanciesResponse mockResponse = mock(HhVacanciesResponse.class);
        HhVacancyItem item = mock(HhVacancyItem.class);

        when(item.id()).thenReturn("123");
        when(item.name()).thenReturn("Java Developer");
        when(mockResponse.items()).thenReturn(List.of(item));

        when(responseSpec.bodyToMono(HhVacanciesResponse.class)).thenReturn(Mono.just(mockResponse));
        when(responseSpec.bodyToMono(HhVacancyItem.class)).thenReturn(Mono.just(item));

        List<RealVacancy> result = parserService.getVacancies("java", "1", 1);

        assertFalse(result.isEmpty());
        assertEquals("Java Developer", result.get(0).getNameOfVacancy());
    }
}
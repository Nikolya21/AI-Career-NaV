package parserTest;

import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.parser.ParserService;
import org.example.aicareernav1.service.parser.SelectVacancy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectVacancyTest {

    @Mock private ParserService parserService;
    @Mock private GigaChatService gigaChatService;

    @InjectMocks
    private SelectVacancy selectVacancy;

    @Test
    void extractThreeVacancies_ShouldParseCorrectly() {
        String aiAnswer = "Вот ваши вакансии\n::: Java Developer, QA, Python Dev";

        List<String> result = selectVacancy.extractThreeVacancies(aiAnswer, 0);

        assertEquals(3, result.size());
        assertEquals("Java Developer", result.get(0));
        assertEquals("Python Dev", result.get(2));
    }

    @Test
    void validateAndFixResponse_ShouldCallGigaChatWhenFormatIsBroken() {
        String brokenResponse = "Плохой ответ без двоеточий";
        when(gigaChatService.sendMessage(anyString())).thenReturn("::: Fixed1, Fixed2, Fixed3");

        String result = selectVacancy.validateAndFixResponse(brokenResponse);

        assertTrue(result.startsWith(":::"));
        verify(gigaChatService, times(1)).sendMessage(anyString());
    }
}
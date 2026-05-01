package quizTest;

import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.promptService.AiPromptProvider;
import org.example.aicareernav1.service.testService.LanguageDetectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LanguageDetectionServiceTest {
    @Mock
    private GigaChatService gigaChatService;
    @Mock
    private AiPromptProvider promptProvider;
    @InjectMocks
    private LanguageDetectionService languageDetectionService;

    @Test
    void shouldReturnCleanedLanguageWhenSupported() {
        when(promptProvider.getLanguageDetectionPrompt(anyString())).thenReturn("test prompt");
        when(gigaChatService.sendMessage(anyString())).thenReturn(" [Java]\n ");
        String result = languageDetectionService.detectLanguage("Вакансия Java");
        assertEquals("java", result);
    }

    @Test
    void shouldReturnNoneWhenLanguageNotSupported() {
        when(promptProvider.getLanguageDetectionPrompt(anyString())).thenReturn("test prompt");
        when(gigaChatService.sendMessage(anyString())).thenReturn("UnknownLang");
        String result = languageDetectionService.detectLanguage("Вакансия");
        assertEquals("none", result);
    }
}

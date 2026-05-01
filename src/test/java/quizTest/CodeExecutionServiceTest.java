package quizTest;

import org.example.aicareernav1.dto.testDto.CodeExecutionResult;
import org.example.aicareernav1.service.testService.CodeExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CodeExecutionServiceTest {
    @InjectMocks
    private CodeExecutionService codeExecutionService;


    @Test
    void shouldReturnErrorForUnsupportedLanguage() {
        CodeExecutionResult result = codeExecutionService.execute("print(1)", "pascal");
        assertEquals("Unsupported language: pascal", result.getStderr());
        assertEquals("none", result.getDetectedLanguage());
    }

    @Test
    void shouldReturnErrorForNoneLanguage() {
        CodeExecutionResult result = codeExecutionService.execute("code", "none");
        assertTrue(result.getStderr().contains("Compiler not available"));
    }
}

package roadmapTest;

import org.example.aicareernav1.service.roadmap.CheckpointService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CheckpointUtilsTest {

    private final CheckpointService service = new CheckpointService(null, null, null, null, null, null, null, null, null, null, null, null, null);

    @ParameterizedTest
    @CsvSource({
      "'Разработка на языке Java', 'Разработка на языке...'"
    })
    void sanitizeTitle_ShouldHandleVariousCases(String input, String expected) {
        // Используем Reflection для доступа к приватному методу
        String result = ReflectionTestUtils.invokeMethod(service, "sanitizeTitle", input, 3);
        assertEquals(expected, result);
    }
}

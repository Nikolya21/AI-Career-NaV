package parserTest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.example.aicareernav1.service.parser.HhOAuthService;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.io.IOException;

class HhOAuthServiceTest {

    private static MockWebServer mockBackEnd;
    private HhOAuthService oauthService;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        oauthService = new HhOAuthService();
        // Подставляем значения через Reflection, так как @Value в тестах не работает без контекста
        ReflectionTestUtils.setField(oauthService, "clientId", "test-id");
        ReflectionTestUtils.setField(oauthService, "clientSecret", "test-secret");
        ReflectionTestUtils.setField(oauthService, "tokenUri", mockBackEnd.url("/oauth/token").toString());
    }

    @Test
    void getAccessToken_ShouldFetchNewTokenSuccessfully() {
        // Подготавливаем фейковый ответ от hh.ru
        mockBackEnd.enqueue(new MockResponse()
          .setBody("{\"access_token\": \"fake-token\", \"expires_in\": 3600}")
          .addHeader("Content-Type", "application/json"));

        StepVerifier.create(oauthService.getAccessToken())
          .expectNext("fake-token")
          .verifyComplete();
    }
}
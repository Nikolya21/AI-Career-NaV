package org.example.aicareernav1.service.testService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.promptService.AiPromptProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageDetectionService {

    private final GigaChatService gigaChatService;
    private final AiPromptProvider promptProvider;

    public String detectLanguage(String vacancy) {
        String prompt = promptProvider.getLanguageDetectionPrompt(vacancy);
        log.info("🚀 Отправка промпта в GigaChat для вакансии: {}", vacancy);

        String response = gigaChatService.sendMessage(prompt);
        log.info("📥 Сырой ответ от GigaChat: [{}]", response);

        String cleanedResponse = response.trim().toLowerCase().replaceAll("[^a-z+#]", "");
        log.info("🧹 Очищенный ответ: [{}]", cleanedResponse);

        boolean isSupported = AiPromptProvider.SUPPORTED_LANGUAGES.contains(cleanedResponse);
        log.info("✅ Язык поддерживается? {}", isSupported);

        return isSupported ? cleanedResponse : "none";
    }
}
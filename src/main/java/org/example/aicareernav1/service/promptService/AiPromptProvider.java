package org.example.aicareernav1.service.promptService;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AiPromptProvider {

    // Список языков, для которых у нас готовы Docker-песочницы
    public static final Set<String> SUPPORTED_LANGUAGES = Set.of(
      "java", "python", "cpp", "javascript", "go", "php", "ruby", "c#"
    );

    public String getLanguageDetectionPrompt(String vacancy) {
        return String.format(
          "Analyze the job vacancy: '%s'. " +
            "Your task is to identify the primary programming language for this role. " +
            "Supported languages are: %s. " +
            "If the vacancy matches one of these, return ONLY the language name in lowercase. " +
            "If the vacancy does NOT require coding in these languages, or it's a non-tech role, return 'none'. " +
            "Return only one word.",
          vacancy, String.join(", ", SUPPORTED_LANGUAGES)
        );
    }
}

package org.example.aicareernav1.service.util;


import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LlmResponseParserService {

    @Getter
    @Builder
    public static class ParsedLlmContent {
        private final List<String> tags;
        private final String content;
        private final String summary;
    }


    /**
     * Извлекает блоки "Чему ты научишься" и "Итоги раздела" для создания summary.
     */
    public String extractSummary(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        StringBuilder summaryBuilder = new StringBuilder();

        // Регулярное выражение для поиска блоков от заголовка до следующего заголовка или конца текста
        // Группа 1: Название секции, Группа 2: Контент секции
        Pattern sectionPattern = Pattern.compile(
                "(###\\s*(?:🎯\\s*Чему ты научишься|✅\\s*Итоги раздела))([\\s\\S]*?)(?=###|$)"
        );

        Matcher matcher = sectionPattern.matcher(content);

        while (matcher.find()) {
            String sectionTitle = matcher.group(1).trim();
            String sectionContent = matcher.group(2).trim();

            summaryBuilder.append(sectionTitle).append("\n")
                    .append(sectionContent).append("\n\n");
        }

        return summaryBuilder.toString().trim();
    }

    /**
     * Разбирает ответ LLM, отделяя метаданные от основного контента.
     * Формат: METADATA: tag1, tag2 === Content
     */
    public ParsedLlmContent parseTheoryResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return ParsedLlmContent.builder()
                    .tags(new ArrayList<>())
                    .content("")
                    .build();
        }

        String markdownContent = rawResponse;
        List<String> tags = new ArrayList<>();

        if (rawResponse.contains("===")) {
            String[] parts = rawResponse.split("===", 2);
            String header = parts[0];
            markdownContent = parts[1].trim();

            String upperHeader = header.toUpperCase();
            if (upperHeader.contains("METADATA:")) {
                try {
                    int startIndex = upperHeader.indexOf("METADATA:") + 9;
                    String tagsString = header.substring(startIndex).trim();

                    tags = Arrays.stream(tagsString.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.error("Ошибка при парсинге тегов из заголовка: {}", header);
                }
            }
        } else {
            log.warn("Разделитель '===' не найден в ответе LLM. Весь текст будет считаться контентом.");
        }

        // 2. Извлекаем summary из чистого контента (без удаления блоков из текста)
        String summary = extractSummary(markdownContent);
        return ParsedLlmContent.builder()
                .tags(tags)
                .content(markdownContent)
                .summary(summary)
                .build();
    }

    /**
     * Извлекает заголовок из сгенерированного ответа.
     * Ищет первую строку, начинающуюся с ## (Markdown заголовок).
     */
    public String getTitleFromTheoryResponse(String rawResponse) {
        // Сначала отсекаем метаданные, если они есть
        String content = rawResponse.contains("===")
                ? rawResponse.split("===")[1].trim()
                : rawResponse.trim();

        return Arrays.stream(content.split("\\n"))
                .filter(line -> line.trim().startsWith("##"))
                .findFirst()
                .map(line -> line.replace("##", "").trim())
                .orElse("Новый урок"); // Fallback, если заголовок не найден
    }
}

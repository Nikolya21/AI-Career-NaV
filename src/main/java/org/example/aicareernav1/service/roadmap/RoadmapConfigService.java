package org.example.aicareernav1.service.roadmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.embeddable.RoadmapConfig;
import org.example.aicareernav1.service.roadmap.theory.prompt.Prompts;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapConfigService {

    /**
     * Генерирует промпт для адаптации поискового запроса на основе конфигурации.
     */
    public String getAdaptatedPrompt(String userQuery, RoadmapConfig config) {
        if (config == null) {
            // Создаем дефолтный конфиг, если его нет, чтобы промпт не развалился
            log.warn("RoadmapConfig is missing! Using default config.");
            config = createDefaultConfig();
        }
        return Prompts.getQueryAdaptationForSearchPrompt(userQuery, config);
    }

    /**
     * Превращает конфиг в плоскую строку тегов (для RAG или логирования).
     */
    public String getFormattedTags(RoadmapConfig config) {
        if (config == null) {
            config = createDefaultConfig();
        }

        List<String> tags = new ArrayList<>();

        // 1. Домен (Java, Python и т.д.)
        tags.add(config.getMainDomain() != null ? config.getMainDomain() : "IT");

        // 2. Уровень
        tags.add(config.getTargetLevel() != null ? config.getTargetLevel() : "Junior");

        // 3. Стиль обучения
        if (config.getLearningStyle() != null && !config.getLearningStyle().isBlank()) {
            tags.add(config.getLearningStyle());
        }

        // Ограничиваем количество тегов согласно лимиту в конфиге
        int limit = (config.getMaxTags() != null) ? config.getMaxTags() : 5;

        return tags.stream()
                .limit(limit)
                .collect(Collectors.joining(", "));
    }

    /**
     * Вспомогательный метод для создания настроек по умолчанию.
     */
    private RoadmapConfig createDefaultConfig() {
        return RoadmapConfig.builder()
                .mainDomain("Software Engineering")
                .targetLevel("Beginner")
                .learningStyle("Standard technical explanation")
                .maxTags(5)
                .build();
    }
}
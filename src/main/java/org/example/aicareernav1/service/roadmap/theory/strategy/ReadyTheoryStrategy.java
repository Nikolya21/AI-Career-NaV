package org.example.aicareernav1.service.roadmap.theory.strategy;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.external.pythonRAG.GatewayResponse;
import org.example.aicareernav1.dto.external.pythonRAG.SearchRequest;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Lesson;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Resource;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Theory;
import org.example.aicareernav1.enums.ResourceType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ReadyTheoryStrategy implements TheoryProcessingStrategy {

    @Override
    public boolean supports(String status) {
        return "READY_LESSON".equals(status) && false; //todo: в будущем улучшить семантический поиск...
    }


    @Override
    public Theory process(GatewayResponse response, SearchRequest request, Lesson lesson, RoadmapConfig config, String context) {
        // Вызываем очистку текста перед созданием сущности
        String cleanedText = cleanDuplicateHeadings(response.getContent());

        Theory theory = Theory.builder()
                .text(cleanedText)
                .tags(List.of("FINISH IT"))
                .externalId(response.getParentId())
                .lesson(lesson)
                .resources(new ArrayList<>())
                .build();

        // Превращаем список строк из Python в ваши сущности Resource
        if (response.getResources() != null) {
            response.getResources().forEach(url -> {
                theory.getResources().add(Resource.builder()
                        .url(url)
                        .title("Справочный материал")
                        .type(ResourceType.ARTICLE)
                        .theory(theory)
                        .build());
            });
        }

        lesson.setTheory(theory);
        return theory;
    }

    /**
     * Удаляет идущие подряд одинаковые Markdown заголовки.
     * Например: "### Основная часть \n ### Основная часть" превратится в один заголовок.
     */
    private String cleanDuplicateHeadings(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String[] lines = content.split("\\r?\\n");
        StringBuilder cleaned = new StringBuilder();
        String lastNormalizedLine = "";

        for (String line : lines) {
            // Убираем Markdown-символы, пробелы и приводим к регистру для сравнения
            String normalized = line.replaceAll("[#*\\s\\-]", "").toLowerCase();

            // Если строка пустая (были только решетки или пробелы) — просто добавляем
            if (normalized.isEmpty()) {
                cleaned.append(line).append("\n");
                continue;
            }

            // Если текущая нормализованная строка совпадает с предыдущей — игнорируем её
            if (normalized.equals(lastNormalizedLine)) {
                continue;
            }

            cleaned.append(line).append("\n");
            lastNormalizedLine = normalized;
        }

        return cleaned.toString().trim();
    }

}

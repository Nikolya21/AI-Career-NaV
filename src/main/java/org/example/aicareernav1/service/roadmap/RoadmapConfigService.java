package org.example.aicareernav1.service.roadmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadmap.config.RoadmapConfigUpdateDTO;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.Roadmap;
import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;
import org.example.aicareernav1.mapper.RoadmapConfigMapper;
import org.example.aicareernav1.repository.roadmap.RoadmapRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.util.JsonUtilsService;
import org.example.aicareernav1.service.roadmap.prompt.RoadmapConfigPrompts;
import org.example.aicareernav1.service.yandexGpt.YandexGptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapConfigService {

    private final RoadmapRepository roadmapRepository;
    private final ObjectMapper objectMapper;
    private final RoadmapConfigMapper configMapper;
    private final YandexGptService llmService;
    private final JsonUtilsService jsonUtils;

    /**
     * Обновление конфигурации на основе сообщения пользователя.
     * Использует LLM для извлечения тегов и JsonUtils для безопасного парсинга.
     */
    @Transactional
    public void updateConfigFromUserText(Roadmap roadmap, String userMessage) {
        RoadmapConfig currentConfig = roadmap.getConfig();
        if (currentConfig == null) {
            currentConfig = createDefaultConfig();
            roadmap.setConfig(currentConfig);
        }

        try {
            log.info("CURRENT Roadmap Config: {}", currentConfig.toString());
            // 1. Превращаем текущий конфиг в JSON строку
            String currentConfigJson = objectMapper.writeValueAsString(currentConfig);

            // 2. Формируем промпт через специальный метод в Prompts
            String prompt = RoadmapConfigPrompts.getConfigExtractorPrompt(currentConfigJson, userMessage);

            // 3. Отправляем в LLM и получаем ответ
            String rawResponse = llmService.sendMessage(prompt);

            String cleanedResponse = jsonUtils.cleanJsonResponse(rawResponse);

            log.info("Roadmap Config to Update JSON: {}", cleanedResponse);
            // 4. Безопасно парсим через твой JsonUtilsService
            RoadmapConfigUpdateDTO updateDto = jsonUtils.parseObject(cleanedResponse, RoadmapConfigUpdateDTO.class);

            if (updateDto != null) {
                // Применяем изменения точечно
                configMapper.updateEntityFromDto(updateDto, roadmap.getConfig());
                roadmapRepository.save(roadmap);
                log.info("Config successfully updated for Roadmap ID {}. New data: {}", roadmap.getId(), cleanedResponse);
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize current config to JSON for Roadmap ID: {}", roadmap.getId());
        } catch (Exception e) {
            log.error("Critical error during config update for Roadmap ID: {}", roadmap.getId(), e);
        }
    }

    /**
     * Точечное обновление полей, чтобы не затереть существующие данные null-значениями
     */
    private void applyChanges(RoadmapConfig existing, RoadmapConfig incoming) {

        if (incoming.getMainDomain() != null) {
            existing.setMainDomain(incoming.getMainDomain());
        }
        if (incoming.getTargetLevel() != null) existing.setTargetLevel(incoming.getTargetLevel());
        if (incoming.getLearningStyle() != null) existing.setLearningStyle(incoming.getLearningStyle());
        if (incoming.getToneOfVoice() != null) existing.setToneOfVoice(incoming.getToneOfVoice());
        // maxTags обычно не меняется юзером через текст, но на всякий случай:
        if (incoming.getMaxTags() != null) existing.setMaxTags(incoming.getMaxTags());
    }

    /**
     * Формирует детализированную строку контекста для LLM.
     * Сочетает в себе строгую структуру (Key: Value) и защиту от пустых данных.
     */
    public String getFullContextString(RoadmapConfig config) {
        // 1. Если конфиг отсутствует целиком, берем дефолтный объект
        if (config == null) {
            config = createDefaultConfig();
        }

        // 2. Извлекаем значения с защитой от null и пустых строк (isBlank)
        String domain = isNotBlank(config.getMainDomain()) ? config.getMainDomain() : "IT";
        String level = isNotBlank(config.getTargetLevel()) ? config.getTargetLevel() : "Junior";
        String style = isNotBlank(config.getLearningStyle()) ? config.getLearningStyle() : "Standard technical explanation";
        String tone = isNotBlank(config.getToneOfVoice()) ? config.getToneOfVoice() : "Neutral mentor";

        // 3. Возвращаем структурированную строку, которая "прошивает" контекст в мозги ИИ
        return String.format(
                "Domain: %s, Level: %s, Style: %s, Tone: %s",
                domain, level, style, tone
        );
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    public RoadmapConfig createDefaultConfig() {
        return RoadmapConfig.builder()
                .mainDomain("Software Engineering")
                .targetLevel("Standart")
                .learningStyle("Standard technical explanation")
                .toneOfVoice("Neutral mentor")
                .maxTags(5)
                .build();
    }
}
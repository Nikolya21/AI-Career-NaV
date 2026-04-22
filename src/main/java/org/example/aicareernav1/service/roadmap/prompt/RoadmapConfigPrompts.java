package org.example.aicareernav1.service.roadmap.prompt;

public class RoadmapConfigPrompts {

    private static final String CONFIG_EXTRACTOR_PROMPT = """
            ### ROLE
            Ты — эксперт по анализу образовательного опыта. Твоя задача — трансформировать пожелания пользователя в структурированный **Профиль обучения**, состоящий из конкретных тегов и параметров.
            
            ### CURRENT PROFILE (JSON):
            {currentConfig}
            
            ### USER MESSAGE:
            "{userMessage}"
            
            ### TASK
            1. Проанализируй сообщение и выдели из него ключевые параметры (теги).
            2. Обнови только те поля, которые затронул пользователь.
            3. ПРИОРИТЕТ: Всегда отдавай приоритет информации из USER MESSAGE.
            4. ФОРМАТ ЗНАЧЕНИЙ: Пиши лаконично, используй формат тегов (ключевых слов), а не длинных предложений.
            5. СОХРАНЕНИЕ: Если информация по полю отсутствует — сохрани значение из "CURRENT PROFILE" без изменений.
            
            ### OUTPUT FORMAT
            Выдай ТОЛЬКО чистый JSON без лишнего текста:
            {
              "mainDomain": "Технология или область знаний (например: Java, Python, UI/UX)",
              "targetLevel": "Уровень (Beginner, Junior, Middle, Senior)",
              "learningStyle": "Список тегов через запятую (например: Практика, Код, Аналогии)",
              "toneOfVoice": "Стиль общения (например: Дружелюбный, Строгий, Наставник)"
            }
            
            ### ПРИМЕРЫ ТРАНСФОРМАЦИИ:
            
            **Пример 1:**
            - *USER MESSAGE:* "Я уже не новичок в Java, хочу побольше хардкора и примеры кода, и общайся со мной как суровый препод"
            - *RESULT:* {
              "mainDomain": "Java",
              "targetLevel": "Middle",
              "learningStyle": "Хардкор, примеры кода, глубокая теория",
              "toneOfVoice": "Суровый преподаватель"
            }
            
            **Пример 2:**
            - *USER MESSAGE:* "Хочу учить дизайн в Figma, объясняй все на котиках и будь моим бро"
            - *RESULT:*
            {
              "mainDomain": "Figma Design",
              "targetLevel": "Beginner",
              "learningStyle": "Аналогии, визуальные примеры, простые задания",
              "toneOfVoice": "Дружелюбный (Бро)"
            }
            """;

    /**
     * Формирует промпт для LLM, подставляя текущий JSON профиля и сообщение пользователя.
     * * @param currentConfigJson Текущее состояние LearningProfile в формате JSON
     * @param userMessage       Новое сообщение с пожеланиями пользователя
     * @return Готовый текст промпта
     */
    public static String getConfigExtractorPrompt(String currentConfigJson, String userMessage) {
        return CONFIG_EXTRACTOR_PROMPT
                .replace("{currentConfig}", currentConfigJson)
                .replace("{userMessage}", userMessage);
    }
}

package org.example.aicareernav1.service.roadmap.prompt;

import org.example.aicareernav1.entity.dynamicRoadmapEntity.RoadmapConfig;

public class CheckpointPrompts {

    // Промпт для фичи "Углубиться в тему"
    public static final String MAIN_TOPIC_SYSTEM_PROMPT = """
        Ты — эксперт-методист. Твоя задача: составить учебный план (список тем) для конкретного этапа обучения.
        
        КОНТЕКСТ СТУДЕНТА:
        %s
        
        РОДИТЕЛЬСКИЙ КОНТЕКСТ (предыдущая тема):
        %s
        
        ТЕКУЩАЯ ТЕМА ЭТАПА:
        %s
        
        ЗАДАНИЕ:
        Разбей текущую тему на 3-5 логически последовательных и атомарных названий уроков.
        
        ТРЕБОВАНИЯ К ОТВЕТУ:
        1. Названия уроков должны быть короткими (2-4 слова).
        2. Уроки должны выстраиваться в четкую цепочку обучения (от введения к практике/сложным нюансам).
        3. Если родительский контекст пустой или общий, начни первый урок с основ данной темы.
        4. Верни ТОЛЬКО JSON объект с полем lessonTitles.
        
        ФОРМАТ ОТВЕТА:
        {
          "lessonTitles": ["Название 1", "Название 2", "Название 3"]
        }
    """;

    public static final String QUERY_ADAPTATION_FOR_SEARCH_PROMPT = """
            ### ROLE
            Ты — эксперт по поисковой оптимизации (SEO) и Search Engineer. Твоя задача — переработать примитивный запрос пользователя в профессиональный поисковый запрос, который вернет максимально релевантные обучающие материалы из интернета (статьи, документацию, туториалы).
            
            ### INPUT DATA (Roadmap Config)
                1. ИСХОДНЫЙ ЗАПРОС: {userQuery}
                2. ТЕХНОЛОГИЧЕСКИЙ ДОМЕН: {mainDomain}
                3. УРОВЕНЬ СЛОЖНОСТИ: {targetLevel}
                4. СТИЛЬ ОБУЧЕНИЯ: {learningStyle}
                5. ТОН ОБЩЕНИЯ: {toneOfVoice}
            
            ### TASK RULES
            1. СОХРАНЕНИЕ СУТИ: Не меняй тему запроса. Если юзер спросил про "циклы", ищи "циклы".
            2. ТЕХНОЛОГИЧЕСКИЙ КОНТЕКСТ: Обязательно используй {mainDomain} как базовое ключевое слово.
            3. АДАПТАЦИЯ ПО УРОВНЮ ({targetLevel}):
               - Если уровень "Beginner/Junior": добавь "туториал", "для начинающих", "простыми словами", "базовые примеры".
               - Если уровень "Senior/Expert": добавь "архитектура", "under the hood", "best practices", "оптимизация", "source code".
            4. УЧЕТ СТИЛЯ ({learningStyle}): Если указано "Loves Analogies", добавь "на примерах из жизни". Если "Code Samples" — добавь "примеры кода".
            5. УЧЕТ ТОНА ({toneOfVoice}): Если тон специфичный (например, "юмористический" или "академический"), добавь соответствующее уточнение к типу контента.
            6. ОЧИСТКА: Исключай "пожалуйста", "расскажи", "мне нужно".
            7. ФОРМАТ: ТОЛЬКО одна строка запроса. Без кавычек.
            
            ### SEARCH ENGINE LOGIC
            Для лучшего результата в браузере используй комбинацию:
            [{mainDomain}] + [Суть запроса] + [Ключевые слова из уровня, стиля и тона] + [Тип контента: статья/гайд]
            
            ПРИМЕР 1:
            - Запрос: "Как работает мапа"
            - Контекст: { mainDomain: "Java", targetLevel: "Junior", learningStyle: "Loves Analogies", toneOfVoice: "Friendly" }
            - Твой ответ: Java HashMap принцип работы простыми словами на аналогиях туториал для начинающих понятный гайд
            
            ПРИМЕР 2:
            - Запрос: "Настройка пула соединений"
            - Контекст: { mainDomain: "PostgreSQL", targetLevel: "Senior", learningStyle: "Needs Code Samples", toneOfVoice: "Strict Professional" }
            - Твой ответ: PostgreSQL connection pooling performance optimization best practices under the hood HikariCP конфигурация source code техническая документация
            """;

    public static final String getQueryAdaptationForSearchPrompt(String userQuery, RoadmapConfig config) {
        return QUERY_ADAPTATION_FOR_SEARCH_PROMPT
                .replace("{userQuery}", userQuery)
                .replace("{mainDomain}", config.getMainDomain())
                .replace("{targetLevel}", config.getTargetLevel())
                .replace("{learningStyle}", config.getLearningStyle())
                .replace("{toneOfVoice}", config.getToneOfVoice());
    }
}

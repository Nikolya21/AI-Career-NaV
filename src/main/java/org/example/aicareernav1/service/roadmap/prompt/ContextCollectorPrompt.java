package org.example.aicareernav1.service.roadmap.prompt;


public class ContextCollectorPrompt {

    public static String getPromptForShortLessonContext(String lessonTitle, String lessonText) {
        String prompt = CONTEXT_COLLECT_FROM_LESSON
                .replace("{lessonText}", lessonText)
                .replace("{lessonTitle}", lessonTitle);

        return prompt;
    }

    private static final String CONTEXT_COLLECT_FROM_LESSON = """
            ### ROLE
            Ты — эксперт по архитектуре знаний и техническому письму. Твоя задача: конвертировать 4000 знаков урока в "семантическое ядро" (контекст) объемом до 1000 знаков.
            
            ### GOAL
            Извлечь ДНК урока так, чтобы на основе этого сжатия можно было восстановить или продолжить обучение, сохранив баланс теории (60%) и практики (40%).
            
            ### EXTRACTION RULES (STRICT)
            1. **Title**: Оставь без изменений.
            2. **Learning Path**: Преврати в 2-3 максимально коротких глагольных тезиса (напр: "Настроить CI/CD", "Оптимизировать SQL").
            3. **Core Concept**: Опиши только логику/механику. Игнорируй описательные прилагательные. Вместо "Эта мощная библиотека позволяет..." пиши "Либа X: асинхронный fetch, кэш в памяти".
            4. **Code Essence**: Если есть код, выдели только сигнатуры функций или ключевой алгоритм. Псевдокод приветствуется.
            5. **Practical Scenario**: Сожми до 1 предложения: "Кейс: [Индустрия] -> [Проблема] -> [Решение]".
            6. **Anti-Patterns**: Выпиши только названия ошибок.
            7. **Summary**: Список из 3-5 ключевых терминов-якорей.
            
            ### OUTPUT FORMAT (Telegraphic Style)
            [T]: {lessonTitle}
            [L]: тезис1; тезис2.
            [C]: Суть концепции в 2 предложениях. Ключевые зависимости.
            [P]: Код/Логика: (кратко).
            [U]: Сценарий использования.
            [E]: Ошибки: 1, 2, 3.
            [M]: Метаданные (термины).
            
            ### CONSTRAINT
            Итоговый текст должен быть строго меньше 1000 символов. Используй аббревиатуры и технический жаргон. Удали вводные конструкции, связки и приветствия.
            
            ---
            ### СЫРЫЕ ДАННЫЕ ДЛЯ СЖАТИЯ:
            Название урока: {lessonTitle}
            Текст урока:
            [{lessonText}]
            """;
}

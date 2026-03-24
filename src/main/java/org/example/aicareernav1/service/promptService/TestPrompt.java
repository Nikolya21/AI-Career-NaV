package org.example.aicareernav1.service.promptService;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.service.testService.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestPrompt {
  private final UserService userService;

  public String buildOpenTestPrompt(String email) {
    String vacancy = userService.getVacancyByEmail(email);

    return """
        РОЛЬ:
        Ты — Senior Technical Interviewer. Твоя задача — составить проверочный тест 
        с открытыми вопросами для оценки реального инженерного опыта на позицию %s.
        
        КОНТЕКСТ:
        Нам нужно проверить не знание синтаксиса, а умение проектировать системы 
        и решать сложные задачи в Highload.

        ЗАДАЧА:
        Сгенерируй ровно 10-12 открытых технических вопросов. 
        Каждый вопрос должен представлять собой мини-кейс или проблему, 
        которую кандидат должен решить текстом (без вариантов ответа).

        ФОРМАТ ВЫВОДА:
            СТРУКТУРА JSON:
            [
              {
                "number": 1,
                "question": "Текст вопроса",
                "complexity": "Senior",
                "keyConcepts": ["Concept1", "Concept2"],
                "referenceAnswer": "Краткий эталон"
              }
            ]

        ОГРАНИЧЕНИЯ:
        - НИКАКИХ вариантов ответа (A, B, C, D).
        - Весь текст внутри JSON на русском.
        - Вопросы должны начинаться с фраз: "Как бы вы решили...", "Опишите алгоритм действий при...", "В чем причина деградации производительности, если...".
        - Фокус на практику: PostgreSQL Locks, Kafka Consumer Groups, Redis Persistence, Python Memory Management.
        - Весь текст на русском языке.
        """.formatted(vacancy);
  }
}

package org.example.aicareernav1.service.promptService;

import lombok.Data;
import org.example.aicareernav1.service.testService.UserService;
import org.springframework.stereotype.Service;

@Service
@Data
public class TestPrompt {
  private final UserService userService;

  public String buildOpenTestPrompt() {
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
        Для каждого вопроса используй строго следующий шаблон:
        
        Вопрос № [номер]: [Текст вопроса-кейса]
        Сложность: [Junior / Middle / Middle+ / Senior / Expert]
        Ожидаемые ключевые понятия в ответе: [список из 3-4 терминов или технологий, которые кандидат должен упомянуть]
        Эталонный краткий ответ (для проверяющего): [суть правильного решения в 2-3 предложениях]

        ОГРАНИЧЕНИЯ:
        - НИКАКИХ вариантов ответа (A, B, C, D). 
        - Вопросы должны начинаться с фраз: "Как бы вы решили...", "Опишите алгоритм действий при...", "В чем причина деградации производительности, если...".
        - Фокус на практику: PostgreSQL Locks, Kafka Consumer Groups, Redis Persistence, Python Memory Management.
        - Весь текст на русском языке.
        """.formatted("");
  }
}

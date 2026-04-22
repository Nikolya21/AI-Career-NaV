package org.example.aicareernav1.dto.roadmap;

import lombok.Builder;
import lombok.Data;

/**
 * Результат проверки ответа пользователя.
 */
@Data
@Builder
public class AnswerCheckResult {
  /** Правильно ли ответил пользователь */
  private boolean correct;
  /** Объяснение: что верно, что нет, подсказка для исправления */
  private String explanation;
}
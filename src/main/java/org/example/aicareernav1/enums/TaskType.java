package org.example.aicareernav1.enums;

public enum TaskType {
  SINGLE_CHOICE,  // Вопрос с одним правильным вариантом ответа
  TRUE_FALSE,     // Утверждение: верно или неверно
  MATCHING,       // Сопоставить левый столбец с правым
  FILL_BLANK,     // Заполнить пропуск в предложении
  ORDERING,       // Расставить элементы в правильном порядке
  PRACTICE,       // Текстовое задание/кейс
  CODE_SNIPPET,   // Написание или анализ кода
  OPEN_QUESTION   // Открытый вопрос с проверкой через ИИ
}
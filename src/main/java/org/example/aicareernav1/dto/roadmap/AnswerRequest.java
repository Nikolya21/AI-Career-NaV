package org.example.aicareernav1.dto.roadmap;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос пользователя с ответом на задачу.
 * Поле {@code answer} содержит JSON-структуру, специфичную для каждого типа задачи:
 * <ul>
 *   <li>SINGLE_CHOICE / QUIZ: {@code {"selectedIndex": 1}}</li>
 *   <li>TRUE_FALSE:           {@code {"answer": true}}</li>
 *   <li>MATCHING:             {@code {"pairs": [0, 2, 1]}}</li>
 *   <li>FILL_BLANK:           {@code {"answer": "текст ответа"}}</li>
 *   <li>ORDERING:             {@code {"order": [1, 2, 0]}}</li>
 *   <li>OPEN_QUESTION:        {@code {"answer": "развернутый ответ"}}</li>
 * </ul>
 */
@Data
@NoArgsConstructor
public class AnswerRequest {
  private JsonNode answer;
}
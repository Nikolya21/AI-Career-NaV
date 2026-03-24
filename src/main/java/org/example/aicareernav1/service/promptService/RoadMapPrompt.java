package org.example.aicareernav1.service.promptService;

import org.example.aicareernav1.dto.userPromptDto.UserRoadMapPromptDto;
import org.springframework.stereotype.Service;

@Service
public class RoadMapPrompt {
  private UserRoadMapPromptDto userRoadMapPromptDto;

  public String buildOpenRoadMapPrompt() {
    String testResult = userRoadMapPromptDto.getTestResult();
    String jobRequirements = userRoadMapPromptDto.getJobRequirements();
    String adaptationCourse = userRoadMapPromptDto.getAdaptationCourse();
    return """
      Роль: Методист-архитектор. Составь учебный план строго в JSON.
      Вводные: Цель: %s, Уровень: %s, Время: %s.
      
      ТРЕБОВАНИЯ К КОНТЕНТУ:
      Для каждой недели пропиши строго 5 ключевых пунктов:
      1. Теория (что прочитать).
      2. Практика (что написать/собрать).
      3. Инструменты (что установить/использовать).
      4. Полезная ссылка (книга/статья).
      5. Критерий успеха (что должен уметь в конце недели).

      МАКСИМУМ НЕДЕЛЬ МОЖЕТ БЫТЬ 10 - 12.
      
      СТРУКТУРА JSON:
      {
        "weeks": [
          {
            "week_number": 1,
            "field_1": "Текст пункта 1",
            "field_2": "Текст пункта 2",
            "field_3": "Текст пункта 3",
            "field_4": "Текст пункта 4",
            "field_5": "Текст пункта 5"
          }
        ]
      }
      Выведи ТОЛЬКО чистый JSON.
      """.formatted(testResult, jobRequirements, adaptationCourse);
  }
}

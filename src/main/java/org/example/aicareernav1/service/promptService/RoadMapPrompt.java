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
      Роль: Методист-архитектор. Составь персонализированный учебный план.
      Вводные: Цель: %s, Уровень: %s, Время: %s.
      
      ТРЕБОВАНИЯ:
      1. Сгенерируй план ровно на 4 недели.
      2. Для каждой недели придумай емкую тему (weekTopic).
      3. В каждой неделе создай строго 5 объектов задач в списке 'tasks'.
      4. У каждой задачи должен быть 'type': theory, practice, link или checkpoint.

      СТРОГО СОБЛЮДАЙ JSON-СТРУКТУРУ:
      {
        "weeks": [
          {
            "weekNumber": 1,
            "weekTopic": "Название темы недели",
            "tasks": [
              { "title": "Заголовок", "type": "theory", "content": "Что именно изучить" },
              ... еще 4 задачи ...
            ]
          }
        ]
      }
      Выведи ТОЛЬКО чистый JSON.
      """.formatted(testResult, jobRequirements, adaptationCourse);
  }
}

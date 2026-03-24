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
      Роль: Методист-архитектор. Твоя задача — составить учебный план строго в формате JSON.
      Вводные: Цель: %s, Уровень: %s, Время: %s.
      
      СТРОГО СОБЛЮДАЙ СТРУКТУРУ JSON:
      {
        "weeks": [
          {
            "week_number": 1,
            "field_1": "Теория (названия книг/статей)",
            "field_2": "Практика (что именно сделать)",
            "field_3": "Критерий успеха (что должен уметь)"
          }
        ]
      }
      Выведи ТОЛЬКО чистый JSON без текста до и после.
      """.formatted(testResult, jobRequirements, adaptationCourse);
  }
}

package org.example.aicareernav1.service.promptService;

import org.springframework.stereotype.Service;

@Service
public class RoadMapPrompt {

  public String buildOpenRoadMapPrompt(String testResult, String jobRequirements, String adaptationCourse) {
    return """
      [SYSTEM: JSON_ONLY_MODE]
                                                              Ты — генератор учебных планов. Твой ответ должен содержать ТОЛЬКО валидный JSON-объект.\s
                                                              ЛЮБОЙ ТЕКСТ ДО И ПОСЛЕ JSON ЗАПРЕЩЕН.
      
                                                              ВХОДНЫЕ ДАННЫЕ:
                                                              Навыки: %s, Вакансия: %s, Доступно времени: %s.
      
                                                              ЗАДАЧА:
                                                              Сгенерируй Roadmap на 7 недели. В каждой неделе строго по 5 задач.
      
                                                              ПРАВИЛА ВАЛИДАЦИИ:
                                                              1. Используй строго структуру: {"weeks": [{"weekNumber": 1, "weekTopic": "string", "tasks": [{"title": "string", "type": "theory|practice|link|checkpoint", "content": "short_string"}]}]}.
                                                              2. "content" — строго до 7 слов. Это критично для предотвращения обрыва генерации.
                                                              3. Убедись, что каждая фигурная скобка "}" закрыта. Не используй многоточия.
                                                              4. Выводи весь JSON одной непрерывной строкой или компактным блоком без лишних отступов.
      
                                                              ОТВЕТЬ В ЭТОМ ФОРМАТЕ:
                                                              {
                                                                "weeks": [
                                                                  {
                                                                    "weekNumber": 1,
                                                                    "weekTopic": "Название темы",
                                                                    "tasks": [
                                                                      {"title": "Заголовок 1", "type": "theory", "content": "Краткое описание до 7 слов"},
                                                                      {"title": "Заголовок 2", "type": "practice", "content": "Краткое описание до 7 слов"},
                                                                      {"title": "Заголовок 3", "type": "link", "content": "Краткое описание до 7 слов"},
                                                                      {"title": "Заголовок 4", "type": "theory", "content": "Краткое описание до 7 слов"},
                                                                      {"title": "Заголовок 5", "type": "checkpoint", "content": "Краткое описание до 7 слов"}
                                                                    ]
                                                                  },
                                                                  ... (повтори для недель 2 - 7)
                                                                ]
                                                              }
      """.formatted(testResult, jobRequirements, adaptationCourse);
  }
}

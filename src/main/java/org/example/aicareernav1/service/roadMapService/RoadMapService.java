package org.example.aicareernav1.service.roadMapService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.roadMapDto.RoadmapResponseDto;
import org.example.aicareernav1.entity.roadmapEntity.RoadmapTaskEntity;
import org.example.aicareernav1.entity.roadmapEntity.RoadmapWeekEntity;
import org.example.aicareernav1.entity.userEntity.UserEntity;
import org.example.aicareernav1.repository.RoadmapWeekRepository;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.promptService.RoadMapPrompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadMapService {
  private final GigaChatService gigaChatService;
  private final RoadMapPrompt roadMapPrompt;
  private final RoadmapWeekRepository roadmapWeekRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public List<RoadmapWeekEntity> generateAndSaveRoadMap(Long userId) {
    // 1. Достаем юзера из базы
    UserEntity user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    // 2. Берем данные из его полей и формируем промпт
    String prompt = roadMapPrompt.buildOpenRoadMapPrompt(
      user.getTestResult(),
      user.getJobRequirements(),
      user.getAdaptationCourse()
    );

    // 3. Работаем с нейросетью через retry механизм
    RoadmapResponseDto responseDto = generateWithRetry(prompt, 1);

    // 4. Очищаем старые данные и сохраняем новые
    roadmapWeekRepository.deleteByUserId(userId);

    List<RoadmapWeekEntity> weekEntities = responseDto.getWeeks().stream()
      .map(weekDto -> {
        RoadmapWeekEntity weekEntity = new RoadmapWeekEntity();
        weekEntity.setUserId(userId);
        weekEntity.setWeekNumber(weekDto.getWeekNumber());
        weekEntity.setWeekTopic(weekDto.getWeekTopic());

        List<RoadmapTaskEntity> taskEntities = weekDto.getTasks().stream()
          .map(taskDto -> {
            RoadmapTaskEntity taskEntity = new RoadmapTaskEntity();
            taskEntity.setTitle(taskDto.getTitle());
            taskEntity.setTaskType(taskDto.getType());
            taskEntity.setContent(taskDto.getContent());
            taskEntity.setWeek(weekEntity);
            return taskEntity;
          }).toList();

        weekEntity.setTasks(taskEntities);
        return weekEntity;
      }).toList();

    return roadmapWeekRepository.saveAll(weekEntities);
  }

  public RoadmapResponseDto generateWithRetry(String prompt, int attempt) {
    log.info("Попытка генерации Roadmap #{}", attempt);

    // 1. Получаем сырой ответ от нейронки
    String rawResponse = gigaChatService.sendMessage(prompt);

    // 2. Очищаем от markdown оберток
    String cleanJson = rawResponse.replaceAll("(?s)```json(.*?)```|```(.*?)```", "$1$2").trim();

    // 3. Базовые исправления
    cleanJson = cleanJson.replaceAll(",\\s*]", "]");   // удаляем лишнюю запятую перед ]
    cleanJson = cleanJson.replaceAll(",\\s*}", "}");   // удаляем лишнюю запятую перед }

    // 4. Расширенное исправление JSON
    String fixedJson = deepFixJson(cleanJson);

    // 5. Печатаем для отладки
    log.debug("Исправленный JSON для попытки {}:\n{}", attempt, fixedJson);

    try {
      // 6. Пробуем парсить
      return objectMapper.readValue(fixedJson, RoadmapResponseDto.class);
    } catch (Exception e) {
      log.error("Ошибка парсинга на попытке {}: {}", attempt, e.getMessage());

      if (attempt < 3) {
        // Рекурсия: пробуем еще раз
        return generateWithRetry(prompt, attempt + 1);
      } else {
        throw new RuntimeException("AI не смог выдать валидный JSON после 3 попыток. Последний ответ: " + fixedJson);
      }
    }
  }

  private String deepFixJson(String rawJson) {
    if (rawJson == null) return null;

    String fixed = rawJson;

    // 1. Исправляем проблему с незакрытыми объектами недель
    // Паттерн: после массива задач идет запятая и начало новой недели, но нет закрытия объекта
    fixed = fixed.replaceAll("\\]\\s*,\\s*\\{", "]}, {");

    // 2. Исправляем случай, когда после задач нет запятой, но нет закрытия
    fixed = fixed.replaceAll("\\]\\s*\\{", "]}, {");

    // 3. Добавляем недостающие закрывающие скобки для недель
    // Считаем количество открытых и закрытых скобок для недель
    StringBuilder sb = new StringBuilder(fixed);

    // 4. Проверяем, что каждый объект недели правильно закрыт
    // Ищем все вхождения "tasks": [ ... ] и убеждаемся, что после ] есть }
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"tasks\"\\s*:\\s*\\[([^\\[]*(?:\\[[^\\[]*\\][^\\[]*)*)\\]");
    java.util.regex.Matcher matcher = pattern.matcher(fixed);
    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      String tasksContent = matcher.group(1);
      String replacement = "\"tasks\": [" + tasksContent + "]";
      matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    fixed = result.toString();

    // 5. Восстанавливаем баланс скобок в целом
    int openBraces = 0;
    int openBrackets = 0;
    for (char c : fixed.toCharArray()) {
      if (c == '{') openBraces++;
      else if (c == '}') openBraces--;
      else if (c == '[') openBrackets++;
      else if (c == ']') openBrackets--;
    }

    // Добавляем недостающие закрывающие скобки
    while (openBraces > 0) {
      sb.append("}");
      openBraces--;
    }
    while (openBrackets > 0) {
      sb.append("]");
      openBrackets--;
    }

    fixed = sb.toString();

    // 6. Убеждаемся, что у нас есть корневой объект
    if (!fixed.trim().startsWith("{")) {
      fixed = "{" + fixed;
    }
    if (!fixed.trim().endsWith("}")) {
      fixed = fixed + "}";
    }

    // 7. Дополнительная очистка: удаляем trailing commas перед закрывающими скобками
    fixed = fixed.replaceAll(",\\s*}", "}");
    fixed = fixed.replaceAll(",\\s*]", "]");

    return fixed;
  }
}




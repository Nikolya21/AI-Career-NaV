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

    // 3. Работаем с нейросетью (Логика JSON остается прежней)
    String rawResponse = gigaChatService.sendMessage(prompt);
    String cleanJson = rawResponse.replaceAll("(?s)```json(.*?)```|```(.*?)```", "$1$2").trim();
    cleanJson = cleanJson.replaceAll(",\\s*]", "]");   // удаляем лишнюю запятую перед ]
    cleanJson = cleanJson.replaceAll(",\\s*}", "}");   // удаляем лишнюю запятую перед }
    cleanJson = cleanJson.replaceAll("}\\s*,?\\s*\\{", "}, {"); // Гарантируем правильный разделитель между объектами
    String fixedJson = fixGigaChatJson(cleanJson);

  // Печатаем для проверки
    System.out.println("--- ПЫТАЕМСЯ ПАРСИТЬ ЭТОТ JSON ---");
    System.out.println(fixedJson);

    // 3. Используем рекурсивную генерацию с защитой (Retry)
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

  private String fixGigaChatJson(String rawJson) {
    if (rawJson == null) return null;

    // 1. Исправляем твою конкретную ошибку из лога: когда неделя не закрыта перед следующей
    // Ищем паттерн: ] (конец задач) , { (начало новой недели)
    // И меняем на: ] } , { (добавляем закрывающую скобку недели)
    String fixed = rawJson.replace("], {", "]}, {");

    // 2. Дозакрываем скобки в самом конце, если GigaChat оборвал ответ
    int openBraces = 0;
    int openBrackets = 0;
    for (char c : fixed.toCharArray()) {
      if (c == '{') openBraces++;
      else if (c == '}') openBraces--;
      else if (c == '[') openBrackets++;
      else if (c == ']') openBrackets--;
    }

    StringBuilder sb = new StringBuilder(fixed);
    while (openBraces > 0) { sb.append("}"); openBraces--; }
    while (openBrackets > 0) { sb.append("]"); openBrackets--; }

    return sb.toString();
  }

  public RoadmapResponseDto generateWithRetry(String prompt, int attempt) {
    log.info("Попытка генерации Roadmap #{}", attempt);

    // 1. Получаем сырой ответ от нейронки
    String rawResponse = gigaChatService.sendMessage(prompt);

    // 2. Пытаемся починить "на лету"
    String fixedJson = fixGigaChatJson(rawResponse);

    try {
      // 3. Пробуем парсить
      return objectMapper.readValue(fixedJson, RoadmapResponseDto.class);
    } catch (Exception e) {
      log.error("Ошибка парсинга на попытке {}: {}", attempt, e.getMessage());

      if (attempt < 3) {
        // Рекурсия: пробуем еще раз, чуть уточнив промпт (опционально)
        return generateWithRetry(prompt, attempt + 1);
      } else {
        throw new RuntimeException("AI не смог выдать валидный JSON после 3 попыток. Последний ответ: " + fixedJson);
      }
    }
  }


}




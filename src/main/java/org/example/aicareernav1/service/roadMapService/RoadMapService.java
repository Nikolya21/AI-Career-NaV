package org.example.aicareernav1.service.roadMapService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

    try {
      RoadmapResponseDto responseDto = objectMapper.readValue(cleanJson, RoadmapResponseDto.class);
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

    } catch (JsonProcessingException e) {
      // Если нейронка выдала плохой JSON, мы увидим это в логах
      System.err.println("Ошибка парсинга от GigaChat: " + cleanJson);
      throw new RuntimeException("Ошибка парсинга JSON: " + e.getMessage());
    }
  }
}




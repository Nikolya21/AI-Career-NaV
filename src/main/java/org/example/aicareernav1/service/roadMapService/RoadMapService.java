package org.example.aicareernav1.service.roadMapService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.roadMapDto.RoadmapResponseDto;
import org.example.aicareernav1.entity.roadmapEntity.RoadmapEntity;
import org.example.aicareernav1.repository.RoadmapRepository;
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
  private final RoadmapRepository roadmapRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public void generateAndSaveRoadMap(Long userId) {
    String prompt = roadMapPrompt.buildOpenRoadMapPrompt();
    String rawResponse = gigaChatService.sendMessage(prompt);
    String cleanJson = rawResponse.replaceAll("(?s)```json(.*?)```|```(.*?)```", "$1$2").trim();

    try {
      RoadmapResponseDto responseDto = objectMapper.readValue(cleanJson, RoadmapResponseDto.class);
      roadmapRepository.deleteByUserId(userId);
      List<RoadmapEntity> entities = responseDto.getWeeks().stream()
        .map(week -> RoadmapEntity.builder()
          .userId(userId)
          .weekNumber(week.getWeek_number())
          .field1(week.getField_1())
          .field2(week.getField_2())
          .field3(week.getField_3())
          .build())
        .toList();
      roadmapRepository.saveAll(entities);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Ошибка парсинга ответа от нейросети: " + e.getMessage());
    }
  }
}


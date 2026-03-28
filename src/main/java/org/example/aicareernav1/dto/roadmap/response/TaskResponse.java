package org.example.aicareernav1.dto.roadmap.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskResponse {
  private Long id;
  private String title;
  private String type;
  private JsonNode content; // Отдаем как готовый JSON объект
}

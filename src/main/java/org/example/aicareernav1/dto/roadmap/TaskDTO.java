package org.example.aicareernav1.dto.roadmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.example.aicareernav1.enums.TaskType;

@Data
public class TaskDTO {
  @JsonProperty("task")
  private String title;
  private TaskType type; // Например, SINGLE_CHOICE
  private JsonNode content; // Принимаем как JSON-узел
}

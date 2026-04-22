package org.example.aicareernav1.dto.roadmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи текущего состояния чекпоинта на фронтенд.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckpointStatusDTO {
  /**
   * Строковое представление CheckpointStatus (например, "GENERATING", "ACTIVE")
   */
  private String status;
}

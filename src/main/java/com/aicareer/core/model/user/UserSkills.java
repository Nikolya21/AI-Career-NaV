package com.aicareer.core.model.user;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// данные обучения, прогресс
public class UserSkills {
  private Long id;
  private Long userId;
  private double fullCompliancePercentage; // прогресс по всему курсу
  private Map<String, Double> skillGaps; // проценты по навыкам
  private Instant calculatedAt;

  public void updateTimestamps() {
      calculatedAt = Instant.now();
  }
}

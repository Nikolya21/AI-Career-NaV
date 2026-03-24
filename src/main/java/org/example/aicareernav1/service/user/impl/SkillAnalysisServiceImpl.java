package org.example.aicareernav1.service.user.impl;

import org.example.aicareernav1.service.user.SkillAnalysisService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SkillAnalysisServiceImpl implements SkillAnalysisService {

  @Override
  public Map<String, Object> analyzeSkillLevel(Long userId, String targetPosition) {
    log.info("Analyzing skill level for user {} for target position: {}", userId, targetPosition);

    Map<String, Object> analysis = new HashMap<>();
    analysis.put("compliancePercentage", calculateCompliance(targetPosition));
    analysis.put("skillGaps", identifySkillGaps(targetPosition));
    analysis.put("recommendedActions", generateRecommendations(targetPosition));
    analysis.put("targetPosition", targetPosition);
    analysis.put("analysisDate", LocalDateTime.now());

    return analysis;
  }

  private double calculateCompliance(String targetPosition) {
    Random random = new Random();
    return 30.0 + random.nextDouble() * 50.0;
  }

  private Map<String, Double> identifySkillGaps(String targetPosition) {
    Map<String, Double> gaps = new HashMap<>();
    gaps.put("Java Spring", 0.75);
    gaps.put("Python", 0.60);
    gaps.put("SQL", 0.45);
    gaps.put("Docker", 0.80);
    return gaps;
  }

  private List<String> generateRecommendations(String targetPosition) {
    return Arrays.asList(
        "Learn Java Spring Boot",
        "Practice Python lib:Pytorch",
        "Spend more time for SQL injections",
        "Remind info about Docker"
    );
  }
}
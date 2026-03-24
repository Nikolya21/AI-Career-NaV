package org.example.aicareernav1.service.user;

import java.util.Map;

public interface SkillAnalysisService {
  Map<String, Object> analyzeSkillLevel(Long userId, String targetPosition);
}
package org.example.aicareernav1.dto.analysis;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AnswerAnalysisRequest {
  private Long userId;
  private List<AnswerItem> answers;

  @Data
  public static class AnswerItem {
    private String text;
    private Map<String, Float> emotions;
    private int blinks;
    private int faceTouches;
  }
}
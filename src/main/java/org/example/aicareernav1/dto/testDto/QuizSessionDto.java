package org.example.aicareernav1.dto.testDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizSessionDto {
  private Long userId;
  private Map<String, String> answers;
}

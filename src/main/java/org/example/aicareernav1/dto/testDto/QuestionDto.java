package org.example.aicareernav1.dto.testDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDto implements Serializable {
  private int number;
  private String question;
}

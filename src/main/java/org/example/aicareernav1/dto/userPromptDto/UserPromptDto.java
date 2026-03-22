package org.example.aicareernav1.dto.userPromptDto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPromptDto {
  private long markTest;
  private String llmNetworkView;
  private String wishes;
}

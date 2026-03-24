package org.example.aicareernav1.dto.dialog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aicareernav1.enums.DialogType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
  private Long userId;
  private String message;
  private DialogType dialogType;
  private Long contextId;
}
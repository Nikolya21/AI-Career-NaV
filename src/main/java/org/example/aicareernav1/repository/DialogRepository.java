package org.example.aicareernav1.repository;

import org.example.aicareernav1.dto.dialog.ChatRequest;
import org.example.aicareernav1.dto.dialog.ChatResponse;
import org.example.aicareernav1.dto.dialog.SummaryResponse;
import org.example.aicareernav1.enums.DialogType;

public interface DialogRepository {
  ChatResponse startDialog(Long userId, DialogType dialogType, Long contextId);
  ChatResponse processMessage(ChatRequest request);
  // Сделать выжимку (имеет смысл в основном для INFORMATION, но пусть будет доступно)
  SummaryResponse summarize(Long userId, DialogType dialogType);
}

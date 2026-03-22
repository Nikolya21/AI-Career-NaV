package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.dialog.ChatRequest;
import org.example.aicareernav1.dto.dialog.ChatResponse;
import org.example.aicareernav1.dto.dialog.SummaryResponse;
import org.example.aicareernav1.enums.DialogType;
import org.example.aicareernav1.repository.DialogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dialogs")
@RequiredArgsConstructor
public class DialogController {

  private final DialogRepository dialogRepository;

  // Пример: POST /api/dialogs/start?userId=1&type=INFORMATION
  @PostMapping("/start")
  public ResponseEntity<ChatResponse> start(
    @RequestParam Long userId,
    @RequestParam DialogType type,
    @RequestParam(required = false) Long contextId) {
    return ResponseEntity.ok(dialogRepository.startDialog(userId, type, contextId));
  }

  // POST /api/dialogs/chat
  @PostMapping("/chat")
  public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
    return ResponseEntity.ok(dialogRepository.processMessage(request));
  }

  // GET /api/dialogs/summarize?userId=1&type=INFORMATION
  @GetMapping("/summarize")
  public ResponseEntity<SummaryResponse> summarize(
    @RequestParam Long userId,
    @RequestParam DialogType type) {
    return ResponseEntity.ok(dialogRepository.summarize(userId, type));
  }
}

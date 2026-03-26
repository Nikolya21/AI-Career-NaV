package org.example.aicareernav1.controller;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.dialog.ChatRequest;
import org.example.aicareernav1.dto.dialog.ChatResponse;
import org.example.aicareernav1.dto.dialog.SummaryResponse;
import org.example.aicareernav1.enums.DialogType;
import org.example.aicareernav1.service.dialog.DialogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dialogs")
@RequiredArgsConstructor
public class DialogController {

  private final DialogService dialogService;

  // Пример: POST /api/dialogs/start?userId=1&type=INFORMATION
  @PostMapping("/start")
  public ResponseEntity<ChatResponse> start(
    @RequestParam Long userId,
    @RequestParam DialogType type,
    @RequestParam(required = false) Long contextId) {
    return ResponseEntity.ok(dialogService.startDialog(userId, type, contextId));
  }

  // POST /api/dialogs/chat
  @PostMapping("/chat")
  public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
    return ResponseEntity.ok(dialogService.processMessage(request));
  }

  // GET /api/dialogs/summarize?userId=1&type=INFORMATION
  @GetMapping("/summarize")
  public ResponseEntity<SummaryResponse> summarize(
    @RequestParam Long userId,
    @RequestParam DialogType type) {
    return ResponseEntity.ok(dialogService.summarize(userId, type));
  }

  @GetMapping("/history")
  public ResponseEntity<List<String>> getHistory(
    @RequestParam Long userId,
    @RequestParam DialogType type) {
    return ResponseEntity.ok(dialogService.getHistory(userId, type));
  }
}

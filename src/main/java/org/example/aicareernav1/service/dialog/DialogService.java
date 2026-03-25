package org.example.aicareernav1.service.dialog;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.dto.dialog.ChatRequest;
import org.example.aicareernav1.dto.dialog.ChatResponse;
import org.example.aicareernav1.dto.dialog.SummaryResponse;
import org.example.aicareernav1.enums.DialogType;
import org.example.aicareernav1.service.dialog.prompt.Prompts;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DialogService {

  private final Map<String, List<String>> historyMap = new ConcurrentHashMap<>();
  private final GigaChatService gigaChatService;
  private static final int WINDOW_SIZE = 8;

  public ChatResponse startDialog(Long userId, DialogType dialogType, Long contextId) {
    String key = getKey(userId, dialogType);

    List<String> history = new ArrayList<>();

    // 2. Выбираем приветственное сообщение
    String initialMessage = switch (dialogType) {
      case INFORMATION -> Prompts.WELCOME_MESSAGE;
      case ROADMAP -> Prompts.ROADMAP_INITIAL_QUESTION;
    };

    history.add("AI: " + initialMessage);
    historyMap.put(key, history);

    return new ChatResponse(initialMessage, false);
  }

  public ChatResponse processMessage(ChatRequest request) {
    Long userId = request.getUserId();
    String key = getKey(userId, request.getDialogType());

    List<String> fullHistory = historyMap.computeIfAbsent(key, k -> new ArrayList<>());

    fullHistory.add("User: " + request.getMessage());

    List<String> contextWindow = getContextWindow(fullHistory);

    String systemPrompt = switch (request.getDialogType()) {
      case INFORMATION -> Prompts.INFO_SYSTEM_PROMPT;
      case ROADMAP -> Prompts.ROADMAP_SYSTEM_PROMPT;
    };

    String llmReply = gigaChatService.chat(systemPrompt, contextWindow);

    fullHistory.add("AI: " + llmReply);

    return new ChatResponse(llmReply, false);
  }

  // Новый метод для фронтенда: загрузить историю при обновлении страницы
  public List<String> getHistory(Long userId, DialogType dialogType) {
    String key = getKey(userId, dialogType);
    return new ArrayList<>(historyMap.getOrDefault(key, Collections.emptyList()));
  }

  public SummaryResponse summarize(Long userId, DialogType dialogType) {
    String key = getKey(userId, dialogType);
    List<String> fullHistory = historyMap.getOrDefault(key, Collections.emptyList());

    if (fullHistory.isEmpty()) {
      return new SummaryResponse("История диалога пуста.");
    }

    if (fullHistory.size() < 4) {
      return new SummaryResponse("Слишком мало данных. Пожалуйста, ответьте на вопросы ассистента.");
    }

    String systemPrompt = switch (dialogType) {
      case INFORMATION -> Prompts.SUMMARIZE_INFO_PROMPT;
      case ROADMAP -> Prompts.SUMMARIZE_ROADMAP_PROMPT;
    };

    String llmResult = gigaChatService.summarize(fullHistory, systemPrompt);
    return new SummaryResponse(llmResult);
  }

  private List<String> getContextWindow(List<String> fullHistory) {
    int size = fullHistory.size();
    if (size <= WINDOW_SIZE) {
      return new ArrayList<>(fullHistory);
    }
    return new ArrayList<>(fullHistory.subList(size - WINDOW_SIZE, size));
  }

  private String getKey(Long userId, DialogType dialogType) {
    return userId + "_" + dialogType.name();
  }
}
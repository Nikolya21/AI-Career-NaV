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

  // Хранилище историй: Ключ - userId, Значение - список сообщений
  private final Map<String, List<String>> historyMap = new ConcurrentHashMap<>();

  private final GigaChatService gigaChatService;

  private static final int WINDOW_SIZE = 8;

  public ChatResponse startDialog(Long userId, DialogType dialogType, Long contextId) {
    // Очищаем старую историю при старте нового диалога
    String key = getKey(userId, dialogType);
    historyMap.put(key, new ArrayList<>());

    String initialMessage = switch (dialogType) {
      case INFORMATION -> Prompts.WELCOME_MESSAGE;
      case ROADMAP -> Prompts.ROADMAP_INITIAL_QUESTION;
    };

    return new ChatResponse(initialMessage, false);
  }

  public ChatResponse processMessage(ChatRequest request) {
    Long userId = request.getUserId();
    String key = getKey(userId, request.getDialogType());
    List<String> fullHistory = historyMap.computeIfAbsent(key, k -> new ArrayList<>());

    // Добавляем сообщение пользователя в историю
    fullHistory.add("User: " + request.getMessage());

    // Получаем только "хвост" диалога для отправки в LLM
    List<String> contextWindow = getContextWindow(fullHistory);

    String systemPrompt = switch (request.getDialogType()) {
      case INFORMATION -> Prompts.INFO_SYSTEM_PROMPT;
      case ROADMAP -> Prompts.ROADMAP_SYSTEM_PROMPT;
    };

    // Здесь вызывается твой LLM Client, куда передается systemPrompt + contextWindow
    String llmReply = gigaChatService.chat(systemPrompt, contextWindow);

    // Добавляем ответ LLM в историю
    fullHistory.add("AI: " + llmReply);

    return new ChatResponse(llmReply, false);
  }

  public SummaryResponse summarize(Long userId, DialogType dialogType) {
    String key = getKey(userId, dialogType);
    List<String> fullHistory = historyMap.getOrDefault(key, Collections.emptyList());

    if (fullHistory.isEmpty()) {
      return new SummaryResponse("История диалога пуста.");
    }

    // Собираем историю
    StringBuilder historyBuilder = new StringBuilder();
    for (String msg : fullHistory) {
      historyBuilder.append(msg).append("\n");
    }

    // Выбираем промпт в зависимости от типа диалога
    String systemPrompt = switch (dialogType) {
      case INFORMATION -> Prompts.SUMMARIZE_INFO_PROMPT;
      case ROADMAP -> Prompts.SUMMARIZE_ROADMAP_PROMPT;
    };

    String llmResult = gigaChatService.summarize(fullHistory, systemPrompt);
    /* ПРИМЕР ТОГО, ЧТО ВЕРНЕТ LLM ДЛЯ ROADMAP:
       1. Время: 10 часов в неделю.
       2. Формат: Видео-лекции, есть опыт на Stepik.
       3. Драйвер: Смена профессии, интерес к алгоритмам.
       4. База: Инженерное образование, хобби - шахматы.
    */

    return new SummaryResponse(llmResult);
  }

  private List<String> getContextWindow(List<String> fullHistory) {
    int size = fullHistory.size();
    if (size <= WINDOW_SIZE) {
      return new ArrayList<>(fullHistory);
    }
    // Берем последние WINDOW_SIZE элементов
    return new ArrayList<>(fullHistory.subList(size - WINDOW_SIZE, size));
  }

  private String getKey(Long userId, DialogType dialogType) {
    return userId + "_" + dialogType.name();
  }
}


package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.model.vacancy.RealVacancy;
import org.example.aicareernav1.repository.UserRepository;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.parser.ParserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DialogVacancyController {

  private final GigaChatService gigaChatService;
  private final ParserService parserService;
  private final UserRepository userRepository;

  private static final String SYSTEM_PROMPT = """
        Ты — опытный HR-аналитик и карьерный консультант.
        Твоя задача — провести интервью с пользователем, чтобы понять его опыт, навыки,
        интересы и карьерные цели. На основе этого ты подберешь 3 наиболее подходящие вакансии.
        
        Правила:
        1. Задавай уточняющие вопросы, чтобы лучше понять пользователя
        2. Будь дружелюбным и профессиональным
        3. После 5 вопросов подведи итог и предложи 3 конкретные вакансии
        4. Вакансии должны быть реальными IT-профессиями
        5. Формат предложения вакансий: строго "Вакансия1, Вакансия2, Вакансия3"
        
        Начни диалог с приветствия и первого вопроса о текущем опыте пользователя.
        """;

  // Шаг 1: Начать диалог (GET)
  @GetMapping("/send-message")
  public String startDialog(HttpSession session) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    // Сбрасываем предыдущие данные
    session.removeAttribute("vacancyDiscussionHistory");
    session.removeAttribute("vacancyDiscussionCount");
    session.removeAttribute("vacancyDiscussionCompleted");
    session.removeAttribute("suggestedVacancies");
    session.removeAttribute("selectedVacancyName");

    log.info("🆕 Начинаем новый диалог");

    // Генерируем первое сообщение от AI
    String firstMessage = gigaChatService.chat(SYSTEM_PROMPT, "");

    List<String> discussionHistory = new ArrayList<>();
    discussionHistory.add(firstMessage);
    session.setAttribute("vacancyDiscussionHistory", discussionHistory);
    session.setAttribute("vacancyDiscussionCount", 1);

    return "redirect:/vacancy-discussion";
  }

  // Шаг 2: Показать страницу диалога (GET)
  @GetMapping("/vacancy-discussion")
  public String showDiscussion(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (discussionHistory == null) {
      return "redirect:/send-message";
    }

    model.addAttribute("discussionHistory", discussionHistory);
    model.addAttribute("questionsCount", questionCount != null ? questionCount : 1);
    model.addAttribute("dialogCompleted", false);

    return "DialogService";
  }

  // Шаг 3: Обработать сообщение пользователя (POST) ← ЭТОТ МЕТОД БЫЛ ОТСУТСТВУЕТ!
  @PostMapping("/vacancy-discussion")
  public String processMessage(@RequestParam("message") String message,
      HttpSession session,
      Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (discussionHistory == null) {
      discussionHistory = new ArrayList<>();
    }
    if (questionCount == null) {
      questionCount = 1;
    }

    // Добавляем ответ пользователя
    discussionHistory.add("User: " + message);
    log.info("📝 Ответ пользователя {}: {}", questionCount, message);

    // Если достигли 5 вопросов, завершаем диалог
    if (questionCount >= 5) {
      String finalPrompt = buildFinalPrompt(discussionHistory);
      String aiResponse = gigaChatService.sendMessage(finalPrompt);
      List<String> suggestedVacancies = extractVacancies(aiResponse);

      session.setAttribute("suggestedVacancies", suggestedVacancies);
      session.setAttribute("vacancyDiscussionCompleted", true);
      session.setAttribute("vacancyDiscussionHistory", discussionHistory);

      log.info("🎉 Диалог завершён, предложено вакансий: {}", suggestedVacancies.size());
      return "redirect:/choose-vacancy";
    }

    // Генерируем следующий вопрос через AI
    String nextQuestion = generateNextQuestionWithAI(discussionHistory, questionCount);
    discussionHistory.add("AI: " + nextQuestion);

    questionCount++;
    session.setAttribute("vacancyDiscussionCount", questionCount);
    session.setAttribute("vacancyDiscussionHistory", discussionHistory);

    return "redirect:/vacancy-discussion";
  }

  // Шаг 4: Показать выбор вакансии (GET)
  @GetMapping("/choose-vacancy")
  public String showChooseVacancy(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    // Получаем вакансии из сессии (установлены через API)
    List<String> suggestedVacancies = (List<String>) session.getAttribute("suggestedVacancies");

    if (suggestedVacancies == null || suggestedVacancies.isEmpty()) {
      // Если нет в сессии, используем дефолтные
      suggestedVacancies = Arrays.asList("Java Developer", "Python Developer", "Frontend Developer");
    }

    model.addAttribute("suggestedVacancies", suggestedVacancies);
    model.addAttribute("userEmail", session.getAttribute("userEmail"));

    return "ChooseVacancy";
  }

  @PostMapping("/choose-vacancy")
  public String chooseVacancy(@RequestParam("selectedVacancy") String selectedVacancyName,
                              HttpSession session) {
    if (session.getAttribute("authenticated") == null) {
      log.warn("⚠️ Пользователь не аутентифицирован");
      return "redirect:/login";
    }

    session.setAttribute("selectedVacancyName", selectedVacancyName);

    // Логируем все атрибуты сессии для отладки
    log.info("🔍 Содержимое сессии:");
    log.info("  - authenticated: {}", session.getAttribute("authenticated"));
    log.info("  - userId: {}", session.getAttribute("userId"));
    log.info("  - userEmail: {}", session.getAttribute("userEmail"));
    log.info("  - userName: {}", session.getAttribute("userName"));

    Long userId = (Long) session.getAttribute("userId");
    if (userId != null) {
      log.info("🔍 Ищем пользователя с ID: {}", userId);
      Optional<UserEntity> userOpt = userRepository.findById(userId);
      if (userOpt.isPresent()) {
        UserEntity user = userOpt.get();
        log.info("🔍 Найден пользователь: {}, текущая вакансия: {}", user.getEmail(), user.getVacancyNow());
        user.setVacancyNow(selectedVacancyName);
        userRepository.save(user);
        log.info("✅ Вакансия {} сохранена в БД для пользователя {}", selectedVacancyName, user.getEmail());

        // Проверяем, что сохранилось
        UserEntity savedUser = userRepository.findById(userId).get();
        log.info("✅ Проверка: в БД теперь вакансия: {}", savedUser.getVacancyNow());
      } else {
        log.warn("⚠️ Пользователь с ID {} не найден в БД", userId);
      }
    } else {
      log.warn("⚠️ userId отсутствует в сессии");
    }

    log.info("✅ Пользователь выбрал вакансию: {}", selectedVacancyName);
    return "redirect:/real-vacancies";
  }

  // Шаг 6: Показать реальные вакансии (GET)
  @GetMapping("/real-vacancies")
  public String showRealVacancies(@RequestParam(required = false) String vacancy,
      HttpSession session,
      Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    String selectedVacancy = vacancy;

    // Если параметр не передан, берем из сессии
    if (selectedVacancy == null) {
      selectedVacancy = (String) session.getAttribute("selectedVacancyName");
    }

    // Если всё равно null, перенаправляем на выбор
    if (selectedVacancy == null) {
      return "redirect:/choose-vacancy";
    }

    // Сохраняем в сессию для последующих запросов
    session.setAttribute("selectedVacancyName", selectedVacancy);

    try {
      List<RealVacancy> realVacancies = parserService.getVacancies(selectedVacancy, "1", 10);
      model.addAttribute("selectedVacancy", selectedVacancy);
      model.addAttribute("realVacancies", realVacancies);
      log.info("✅ Найдено {} реальных вакансий для: {}", realVacancies.size(), selectedVacancy);
    } catch (Exception e) {
      log.error("Ошибка при получении реальных вакансий", e);
      model.addAttribute("error", "Ошибка при загрузке вакансий: " + e.getMessage());
      model.addAttribute("selectedVacancy", selectedVacancy);
      model.addAttribute("realVacancies", new ArrayList<>());
    }

    return "RealVacancies";
  }

  // Вспомогательные методы
  private String buildFinalPrompt(List<String> history) {
    StringBuilder sb = new StringBuilder();
    sb.append("На основе нашего диалога подбери 3 наиболее подходящие вакансии для пользователя.\n\n");
    sb.append("История диалога:\n");
    for (String msg : history) {
      sb.append(msg).append("\n");
    }
    sb.append("\nВажно: ответь строго в формате: Вакансия1, Вакансия2, Вакансия3\n");
    sb.append("Только названия вакансий через запятую, без дополнительного текста.");
    return sb.toString();
  }

  private List<String> extractVacancies(String aiResponse) {
    List<String> vacancies = new ArrayList<>();
    String[] lines = aiResponse.split("\n");
    for (String line : lines) {
      if (line.contains(",") && line.split(",").length >= 3) {
        String[] parts = line.split(",");
        for (String part : parts) {
          String vacancy = part.trim();
          if (!vacancy.isEmpty() && vacancies.size() < 3) {
            vacancies.add(vacancy);
          }
        }
        break;
      }
    }

    if (vacancies.isEmpty() && aiResponse.contains(",")) {
      String[] parts = aiResponse.split(",");
      for (String part : parts) {
        String vacancy = part.trim();
        if (!vacancy.isEmpty() && vacancies.size() < 3) {
          vacancies.add(vacancy);
        }
      }
    }

    if (vacancies.isEmpty()) {
      vacancies = Arrays.asList("Java Developer", "Python Developer", "Frontend Developer");
    }

    return vacancies;
  }

  private String generateNextQuestionWithAI(List<String> history, int currentQuestion) {
    StringBuilder context = new StringBuilder();
    context.append("Ты HR-консультант. Задай следующий уточняющий вопрос пользователю.\n\n");
    context.append("История диалога:\n");

    int start = Math.max(0, history.size() - 6);
    for (int i = start; i < history.size(); i++) {
      context.append(history.get(i)).append("\n");
    }

    context.append("\nЗадай вопрос №").append(currentQuestion + 1).append(" из 5.\n");
    context.append("Вопрос должен быть конкретным и помогать понять:\n");
    context.append("- Технические навыки пользователя\n");
    context.append("- Опыт работы\n");
    context.append("- Карьерные цели\n");
    context.append("- Предпочтения в работе\n\n");
    context.append("Верни ТОЛЬКО вопрос, без пояснений.");

    return gigaChatService.sendMessage(context.toString());
  }

  @GetMapping("/ml-interview")
  public String mlInterview() {
    return "ml-interview"; // имя HTML-шаблона
  }

}
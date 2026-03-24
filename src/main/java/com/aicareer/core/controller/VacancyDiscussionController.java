package com.aicareer.core.controller;

import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/vacancy-discussion")
@RequiredArgsConstructor
public class VacancyDiscussionController {

  private final GigaChatService gigaChatService;
  private final RoadmapGenerateService roadmapGenerateService;

  @GetMapping
  public String showDiscussion(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
    if (selectedVacancy == null) {
      return "redirect:/choose-vacancy";
    }

    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted != null && discussionCompleted) {
      log.info("🔁 Диалог уже завершен, перенаправляем на roadmap");
      return "redirect:/career-roadmap";
    }

    List<String> existingHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer existingQuestionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (existingHistory != null && !existingHistory.isEmpty() && existingQuestionCount != null) {
      log.info("🔄 Продолжение существующего диалога. Вопросов: {}", existingQuestionCount);
      setupDiscussionPage(session, model);
      return "VacancyDiscussion";
    }

    log.info("🆕 Инициализация нового диалога для вакансии: {}", selectedVacancy);
    initializeVacancyDiscussion(session, selectedVacancy);
    setupDiscussionPage(session, model);
    return "VacancyDiscussion";
  }

  @PostMapping
  public String processMessage(@RequestParam(value = "message", required = false) String message,
      HttpSession session,
      Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted != null && discussionCompleted) {
      log.warn("⚠️ Попытка отправить сообщение в завершенный диалог");
      return "redirect:/career-roadmap";
    }

    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");

    if (message != null && !message.trim().isEmpty()) {
      return processUserResponse(session, selectedVacancy, message.trim(), model);
    } else {
      setupDiscussionPage(session, model);
      return "VacancyDiscussion";
    }
  }

  private void initializeVacancyDiscussion(HttpSession session, String vacancy) {
    List<String> discussionHistory = new ArrayList<>();
    String welcomeMessage = generateWelcomeMessage(vacancy);
    discussionHistory.add(welcomeMessage);

    session.setAttribute("vacancyDiscussionHistory", discussionHistory);
    session.setAttribute("vacancyDiscussionCount", 1);
    session.setAttribute("currentDiscussionType", "vacancy_discussion");
    session.setAttribute("vacancyDiscussionCompleted", false);

    log.info("🔍 Инициализирован диалог для вакансии: {}", vacancy);
  }

  private String generateWelcomeMessage(String vacancy) {
    try {
      String prompt = "Пользователь выбрал вакансию: " + vacancy +
          ". Начни диалог для обсуждения этой вакансии. " +
          "Задай первый вопрос, который поможет понять:\n" +
          "1. Почему пользователь выбрал именно эту вакансию\n" +
          "2. Какой у него текущий опыт в этой области\n" +
          "3. Какие навыки уже есть, а какие нужно развить\n" +
          "4. Какие карьерные цели\n\n" +
          "Вопрос должен быть конкретным и направляющим. Начни прямо с вопроса.";

      log.info("🤖 Генерация приветственного сообщения...");
      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      log.error("❌ Ошибка при генерации приветственного сообщения", e);
      return "Здравствуйте! Вы выбрали вакансию " + vacancy + ". Расскажите, почему вас заинтересовало это направление?";
    }
  }

  private String processUserResponse(HttpSession session, String vacancy, String userMessage, Model model) {
    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (discussionHistory == null) discussionHistory = new ArrayList<>();
    if (questionCount == null) questionCount = 1;

    log.info("📊 Обработка ответа. Текущий счетчик: {}/5", questionCount);

    discussionHistory.add(userMessage);
    log.info("✅ Ответ пользователя добавлен в историю");

    if (questionCount < 5) {
      log.info("🤖 Генерация вопроса {}", (questionCount + 1));
      String nextQuestion = generateNextQuestion(discussionHistory, vacancy, questionCount);
      discussionHistory.add(nextQuestion);

      session.setAttribute("vacancyDiscussionCount", questionCount + 1);
      session.setAttribute("vacancyDiscussionHistory", discussionHistory);

      setupDiscussionPage(session, model);
      return "VacancyDiscussion";
    } else {
      log.info("🎯 Получен 5-й ответ - завершение диалога");
      session.setAttribute("vacancyDiscussionHistory", discussionHistory);
      completeDiscussion(session, discussionHistory, vacancy);
      log.info("🔄 Перенаправление на career-roadmap");
      return "redirect:/career-roadmap";
    }
  }

  private String generateNextQuestion(List<String> history, String vacancy, int currentQuestion) {
    try {
      StringBuilder context = new StringBuilder();
      context.append("Обсуждаем вакансию: ").append(vacancy).append("\n\n");

      int startIndex = Math.max(0, history.size() - 4);
      for (int i = startIndex; i < history.size(); i++) {
        if (i % 2 == 0) {
          context.append("AI: ").append(history.get(i)).append("\n");
        } else {
          context.append("User: ").append(history.get(i)).append("\n");
        }
      }

      String prompt = context +
          "\nНа основе этого диалога задай следующий уточняющий вопрос (" +
          (currentQuestion + 1) + "/5) для составления персонализированного плана развития к вакансии " +
          vacancy + ". Вопрос должен углублять понимание конкретных потребностей пользователя.";

      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      log.error("❌ Ошибка при генерации вопроса", e);
      return "Расскажите подробнее о вашем опыте в этой области?";
    }
  }

  private void completeDiscussion(HttpSession session, List<String> history, String vacancy) {
    try {
      String fullDiscussionPrompt = buildDiscussionPrompt(history, vacancy);
      session.setAttribute("fullDiscussionPrompt", fullDiscussionPrompt);

      String personalizedPlan = generatePersonalizedPlan(history, vacancy);
      session.setAttribute("personalizedVacancyPlan", personalizedPlan);

      Roadmap detailedRoadmap = generateDetailedRoadmapFromDiscussion(vacancy, history, personalizedPlan, session);
      session.setAttribute("generatedRoadmap", detailedRoadmap);

      session.setAttribute("vacancyDiscussionCompleted", true);

      log.info("✅ Диалог завершен. Roadmap сгенерирован: {} зон", detailedRoadmap.getRoadmapZones().size());

    } catch (Exception e) {
      log.error("❌ Ошибка при завершении диалога", e);
      session.setAttribute("vacancyDiscussionCompleted", true);
    }
  }

  private Roadmap generateDetailedRoadmapFromDiscussion(String vacancy, List<String> history, String personalizedPlan, HttpSession session) {
    try {
      log.info("🎯 Генерация детального roadmap для: {}", vacancy);

      List<Week> weeks = generateWeeksFromDiscussion(vacancy, history, personalizedPlan);
      ResponseByWeek responseByWeek = new ResponseByWeek(weeks);

      String weeksInfo = roadmapGenerateService.gettingWeeksInformation(responseByWeek);
      String zonesAnalysis = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInfo);
      List<RoadmapZone> zones = roadmapGenerateService.splittingWeeksIntoZones(zonesAnalysis, weeks);

      Roadmap roadmap = roadmapGenerateService.identifyingThematicallySimilarZones(zones);

      Long userId = (Long) session.getAttribute("userId");
      roadmap.setUserId(userId != null ? userId : 1L);
      roadmap.updateTimestamps();

      log.info("✅ Детальный roadmap создан: {} зон, {} недель", zones.size(), weeks.size());
      return roadmap;

    } catch (Exception e) {
      log.error("❌ Ошибка при генерации детального roadmap", e);
      return createFallbackRoadmap(vacancy, history, personalizedPlan, session);
    }
  }

  private List<Week> generateWeeksFromDiscussion(String vacancy, List<String> history, String personalizedPlan) {
    try {
      String discussionContext = buildDiscussionContextForWeeks(history, vacancy, personalizedPlan);
      String weeksPrompt = createWeeksGenerationPrompt(discussionContext, vacancy);
      String weeksResponse = gigaChatService.sendMessage(weeksPrompt);
      return parseWeeksFromResponse(weeksResponse, vacancy);
    } catch (Exception e) {
      log.error("❌ Ошибка при генерации недель", e);
      return createDefaultWeeks(vacancy);
    }
  }

  private String createWeeksGenerationPrompt(String discussionContext, String vacancy) {
    return "На основе следующего диалога о вакансии " + vacancy +
        " создай план обучения из 8 недель. Каждая неделя должна иметь:\n" +
        "1. Номер недели (от 1 до 8)\n" +
        "2. Конкретную цель на неделю\n" +
        "3. 2-3 практические задачи\n" +
        "4. Релевантные ссылки на ресурсы\n\n" +
        "Контекст диалога:\n" + discussionContext + "\n\n" +
        "Формат ответа для каждой недели:\n" +
        "WEEK:[номер]\n" +
        "GOAL:[цель недели]\n" +
        "TASKS:[задача 1];[задача 2];[задача 3]\n" +
        "URLS:[url1];[url2]\n" +
        "---\n" +
        "Начни сразу с WEEK:1";
  }

  private List<Week> parseWeeksFromResponse(String response, String vacancy) {
    List<Week> weeks = new ArrayList<>();
    String[] weekBlocks = response.split("---");

    for (String block : weekBlocks) {
      if (block.trim().isEmpty()) continue;

      try {
        Week week = new Week();
        String[] lines = block.trim().split("\n");

        for (String line : lines) {
          if (line.startsWith("WEEK:")) {
            week.setNumber(Integer.parseInt(line.substring(5).trim()));
          } else if (line.startsWith("GOAL:")) {
            week.setGoal(line.substring(5).trim());
          } else if (line.startsWith("TASKS:")) {
            String tasksStr = line.substring(6).trim();
            String[] taskDescriptions = tasksStr.split(";");
            week.setTasks(createTasksFromDescriptions(taskDescriptions));
          }
        }

        week.updateTimestamps();
        weeks.add(week);

      } catch (Exception e) {
        log.error("❌ Ошибка парсинга недели", e);
      }
    }

    if (weeks.isEmpty()) {
      return createDefaultWeeks(vacancy);
    }

    return weeks;
  }

  private List<Task> createTasksFromDescriptions(String[] descriptions) {
    List<Task> tasks = new ArrayList<>();
    for (String desc : descriptions) {
      if (desc.trim().isEmpty()) continue;
      Task task = new Task();
      task.setDescription(desc.trim());
      task.setUrls(getRelevantUrlsForTask(desc.trim()));
      task.updateTimestamps();
      tasks.add(task);
    }
    return tasks;
  }

  private List<String> getRelevantUrlsForTask(String taskDescription) {
    List<String> urls = new ArrayList<>();
    String lower = taskDescription.toLowerCase();
    if (lower.contains("java") || lower.contains("программир")) {
      urls.add("https://habr.com/ru/hub/java/");
      urls.add("https://javarush.com/");
    }
    if (lower.contains("spring")) {
      urls.add("https://spring.io/guides");
      urls.add("https://www.baeldung.com/spring-tutorial");
    }
    if (lower.contains("sql") || lower.contains("баз")) {
      urls.add("https://www.w3schools.com/sql/");
      urls.add("https://sql-academy.org/");
    }
    if (lower.contains("алгоритм")) {
      urls.add("https://leetcode.com/");
      urls.add("https://habr.com/ru/hub/algorithms/");
    }
    if (urls.isEmpty()) {
      urls.add("https://habr.com/ru/");
      urls.add("https://stepik.org/");
    }
    return urls;
  }

  private List<Week> createDefaultWeeks(String vacancy) {
    List<Week> weeks = new ArrayList<>();
    for (int i = 1; i <= 8; i++) {
      Week week = new Week();
      week.setNumber(i);
      week.setGoal("Изучение " + vacancy + " - неделя " + i);
      week.setTasks(createDefaultTasks());
      week.updateTimestamps();
      weeks.add(week);
    }
    return weeks;
  }

  private List<Task> createDefaultTasks() {
    List<Task> tasks = new ArrayList<>();
    Task task1 = new Task();
    task1.setDescription("Изучение теоретического материала");
    task1.setUrls(List.of("https://habr.com/ru/", "https://stepik.org/"));
    task1.updateTimestamps();
    tasks.add(task1);

    Task task2 = new Task();
    task2.setDescription("Практическое упражнение");
    task2.setUrls(List.of("https://leetcode.com/", "https://codewars.com/"));
    task2.updateTimestamps();
    tasks.add(task2);

    Task task3 = new Task();
    task3.setDescription("Мини-проект для закрепления знаний");
    task3.setUrls(List.of("https://github.com/", "https://glitch.com/"));
    task3.updateTimestamps();
    tasks.add(task3);

    return tasks;
  }

  private Roadmap createFallbackRoadmap(String vacancy, List<String> history, String personalizedPlan, HttpSession session) {
    log.info("🔄 Создание fallback roadmap для: {}", vacancy);
    Roadmap roadmap = new Roadmap();
    List<RoadmapZone> zones = new ArrayList<>();

    boolean hasExperience = history.stream()
        .anyMatch(msg -> msg.toLowerCase().contains("опыт") && !msg.toLowerCase().contains("нет опыта"));

    if (!hasExperience) {
      RoadmapZone foundationZone = createRoadmapZone("Основы и введение", 1, "Начальный",
          "Изучение фундаментальных концепций " + vacancy, 1, 3);
      zones.add(foundationZone);
    }

    RoadmapZone practiceZone = createRoadmapZone("Практика и применение", 2, "Средний",
        "Разработка практических навыков для " + vacancy,
        zones.isEmpty() ? 1 : 4,
        zones.isEmpty() ? 6 : 7);
    zones.add(practiceZone);

    RoadmapZone projectsZone = createRoadmapZone("Проекты и портфолио", 3, "Продвинутый",
        "Создание проектов для портфолио",
        practiceZone.getWeeks().get(practiceZone.getWeeks().size()-1).getNumber() + 1,
        practiceZone.getWeeks().get(practiceZone.getWeeks().size()-1).getNumber() + 2);
    zones.add(projectsZone);

    roadmap.setRoadmapZones(zones);

    Long userId = (Long) session.getAttribute("userId");
    roadmap.setUserId(userId != null ? userId : 1L);
    roadmap.updateTimestamps();

    return roadmap;
  }

  private RoadmapZone createRoadmapZone(String name, int order, String complexity, String goal, int startWeek, int endWeek) {
    RoadmapZone zone = new RoadmapZone();
    zone.setName(name);
    zone.setZoneOrder(order);
    zone.setComplexityLevel(complexity);
    zone.setLearningGoal(goal);
    zone.setWeeks(createWeeksForZone(startWeek, endWeek, name));
    zone.updateTimestamps();
    return zone;
  }

  private List<Week> createWeeksForZone(int startWeek, int endWeek, String zoneName) {
    List<Week> weeks = new ArrayList<>();
    for (int i = startWeek; i <= endWeek; i++) {
      Week week = new Week();
      week.setNumber(i);
      week.setGoal(zoneName + " - неделя " + i);
      week.setTasks(createDefaultTasks());
      week.updateTimestamps();
      weeks.add(week);
    }
    return weeks;
  }

  private String buildDiscussionContextForWeeks(List<String> history, String vacancy, String personalizedPlan) {
    StringBuilder context = new StringBuilder();
    context.append("Вакансия: ").append(vacancy).append("\n\n");
    context.append("Персонализированный план: ").append(personalizedPlan).append("\n\n");
    context.append("Ключевые моменты диалога:\n");
    for (int i = 1; i < history.size(); i += 2) {
      if (i < history.size()) {
        context.append("- ").append(history.get(i)).append("\n");
      }
    }
    return context.toString();
  }

  private String buildDiscussionPrompt(List<String> history, String vacancy) {
    StringBuilder fullPrompt = new StringBuilder();
    fullPrompt.append("ПОЛНАЯ ИСТОРИЯ ДИАЛОГА ДЛЯ ВАКАНСИИ: ").append(vacancy).append("\n\n");
    for (int i = 0; i < history.size(); i++) {
      if (i % 2 == 0) {
        fullPrompt.append("AI (вопрос ").append((i/2) + 1).append("): ").append(history.get(i)).append("\n");
      } else {
        fullPrompt.append("USER (ответ ").append((i/2) + 1).append("): ").append(history.get(i)).append("\n");
      }
      fullPrompt.append("---\n");
    }
    return fullPrompt.toString();
  }

  private String generatePersonalizedPlan(List<String> history, String vacancy) {
    try {
      StringBuilder fullDialog = new StringBuilder();
      fullDialog.append("Диалог об вакансии: ").append(vacancy).append("\n\n");
      for (int i = 0; i < history.size(); i++) {
        if (i % 2 == 0) {
          fullDialog.append("AI: ").append(history.get(i)).append("\n");
        } else {
          fullDialog.append("User: ").append(history.get(i)).append("\n");
        }
      }

      String prompt = "На основе этого диалога создай краткий персонализированный план развития для пользователя.\n" +
          "Вакансия: " + vacancy + "\n" +
          "Полный диалог:\n" + fullDialog + "\n\n" +
          "Создай структурированный план с этапами развития.";

      log.info("🤖 Генерация персонализированного плана...");
      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      log.error("❌ Ошибка при генерации плана", e);
      return "Персонализированный план будет создан на основе ваших ответов.";
    }
  }

  private void setupDiscussionPage(HttpSession session, Model model) {
    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    if (discussionHistory != null) {
      model.addAttribute("discussionHistory", discussionHistory);
    }

    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount");
    if (questionCount != null) {
      model.addAttribute("questionsCount", questionCount);
    }

    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
    if (selectedVacancy != null) {
      model.addAttribute("selectedVacancy", selectedVacancy);
    }

    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted != null && discussionCompleted) {
      model.addAttribute("showRoadmapButton", true);
    }
  }
}
package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.courseModel.Week;
import org.example.aicareernav1.model.roadmap.Roadmap;
import org.example.aicareernav1.model.roadmap.RoadmapZone;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.roadMapService.RoadMapService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class VacancyDiscussionController {

  private final GigaChatService gigaChatService;
  private final RoadMapService roadmapGenerateService;

  private static final String DISCUSSION_SYSTEM_PROMPT = """
        Ты — карьерный консультант. Теперь пользователь выбрал конкретную вакансию.
        Проведи с ним диалог из 5 вопросов, чтобы:
        1. Понять его текущие навыки и уровень
        2. Выяснить, какие технологии он знает
        3. Узнать его карьерные цели
        4. Понять, сколько времени он готов уделять обучению
        5. Выяснить его предпочтения по формату обучения
        
        На основе ответов ты создашь персонализированный план развития.
        Начни с приветствия и первого вопроса.
        """;

  // Показать страницу второго диалога
  @GetMapping("/vacancy-discussion-2")
  public String showDiscussion(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
    if (selectedVacancy == null) {
      return "redirect:/choose-vacancy";
    }

    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory2");
    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount2");

    if (discussionHistory == null) {
      discussionHistory = new ArrayList<>();
      String welcomeMessage = generateWelcomeMessage(selectedVacancy);
      discussionHistory.add(welcomeMessage);
      session.setAttribute("vacancyDiscussionHistory2", discussionHistory);
      session.setAttribute("vacancyDiscussionCount2", 1);
    }

    model.addAttribute("discussionHistory", discussionHistory);
    model.addAttribute("questionsCount", questionCount != null ? questionCount : 1);
    model.addAttribute("selectedVacancy", selectedVacancy);
    model.addAttribute("dialogType", "vacancy");

    return "VacancyDiscussion";
  }

  // Обработать сообщение во втором диалоге
  @PostMapping("/vacancy-discussion-2")
  public String processMessage(@RequestParam("message") String message,
      HttpSession session,
      Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory2");
    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount2");

    if (discussionHistory == null) {
      discussionHistory = new ArrayList<>();
    }
    if (questionCount == null) {
      questionCount = 1;
    }

    // Добавляем ответ пользователя
    discussionHistory.add("User: " + message);
    log.info("📝 Второй диалог - ответ {}: {}", questionCount, message);

    // Если достигли 5 вопросов, завершаем диалог и генерируем roadmap
    if (questionCount >= 5) {
      String selectedVacancy = (String) session.getAttribute("selectedVacancyName");

      // Генерируем roadmap на основе диалога
      Roadmap roadmap = generateRoadmap(discussionHistory, selectedVacancy, session);
      session.setAttribute("generatedRoadmap", roadmap);
      session.setAttribute("vacancyDiscussionCompleted", true);

      log.info("🎉 Второй диалог завершён, roadmap сгенерирован");
      return "redirect:/career-roadmap";
    }

    // Генерируем следующий вопрос через AI
    String nextQuestion = generateNextQuestion(discussionHistory, questionCount);
    discussionHistory.add("AI: " + nextQuestion);

    questionCount++;
    session.setAttribute("vacancyDiscussionCount2", questionCount);
    session.setAttribute("vacancyDiscussionHistory2", discussionHistory);

    return "redirect:/vacancy-discussion-2";
  }

  private String generateWelcomeMessage(String vacancy) {
    try {
      String prompt = "Пользователь выбрал вакансию: " + vacancy +
          ". Начни диалог для составления персонального плана развития. " +
          "Задай первый вопрос о его текущем опыте и навыках.";
      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      return "Здравствуйте! Вы выбрали вакансию " + vacancy +
          ". Расскажите о вашем текущем опыте в этой области.";
    }
  }

  private String generateNextQuestion(List<String> history, int currentQuestion) {
    try {
      StringBuilder context = new StringBuilder();
      context.append("Ты карьерный консультант. Задай следующий уточняющий вопрос.\n\n");
      context.append("История диалога:\n");

      int start = Math.max(0, history.size() - 6);
      for (int i = start; i < history.size(); i++) {
        context.append(history.get(i)).append("\n");
      }

      context.append("\nЗадай вопрос №").append(currentQuestion + 1).append(" из 5.\n");
      context.append("Вопрос должен помогать понять:\n");
      context.append("- Уровень знаний и навыков\n");
      context.append("- Карьерные цели\n");
      context.append("- Готовность к обучению\n\n");
      context.append("Верни ТОЛЬКО вопрос, без пояснений.");

      return gigaChatService.sendMessage(context.toString());
    } catch (Exception e) {
      String[] defaultQuestions = {
          "Какой у вас текущий уровень знаний в этой области?",
          "Какие технологии вы уже знаете?",
          "Сколько времени в неделю вы готовы уделять обучению?",
          "Какие у вас карьерные цели на ближайшие 2-3 года?",
          "Какой формат обучения вам предпочтителен?"
      };
      return defaultQuestions[currentQuestion];
    }
  }

  private Roadmap generateRoadmap(List<String> history, String vacancy, HttpSession session) {
    try {
      // Собираем контекст для генерации roadmap
      StringBuilder userInfo = new StringBuilder();
      for (String msg : history) {
        if (msg.startsWith("User:")) {
          userInfo.append(msg.substring(5)).append("\n");
        }
      }

      String prompt = buildRoadmapPrompt(userInfo.toString(), vacancy);
      String roadmapResponse = gigaChatService.sendMessage(prompt);

      // Парсим ответ и создаём Roadmap
      Roadmap roadmap = parseRoadmapResponse(roadmapResponse, vacancy);

      Long userId = (Long) session.getAttribute("userId");
      roadmap.setUserId(userId != null ? userId : 1L);
      roadmap.updateTimestamps();

      return roadmap;
    } catch (Exception e) {
      log.error("Ошибка при генерации roadmap", e);
      return createFallbackRoadmap(vacancy);
    }
  }

  private String buildRoadmapPrompt(String userInfo, String vacancy) {
    return """
            На основе информации о пользователе создай детальный план обучения для вакансии %s.
            
            Информация о пользователе:
            %s
            
            Создай план на 8 недель, разбитый на 3 зоны:
            1. Зона 1: Основы и фундамент (недели 1-2)
            2. Зона 2: Углубленное изучение (недели 3-5)
            3. Зона 3: Практика и проекты (недели 6-8)
            
            Для каждой недели укажи:
            - Цель недели
            - 2-3 конкретные задачи
            - Релевантные ресурсы (ссылки)
            
            Формат ответа:
            ZONE:Название зоны
            WEEK:1
            GOAL:цель недели
            TASKS:задача1;задача2;задача3
            URLS:url1;url2
            ---
            """.formatted(vacancy, userInfo);
  }

  private Roadmap parseRoadmapResponse(String response, String vacancy) {
    Roadmap roadmap = new Roadmap();
    List<RoadmapZone> zones = new ArrayList<>();

    String[] zoneBlocks = response.split("ZONE:");
    for (String zoneBlock : zoneBlocks) {
      if (zoneBlock.trim().isEmpty()) continue;

      RoadmapZone zone = new RoadmapZone();
      String[] lines = zoneBlock.split("\n");
      zone.setName(lines[0].trim());

      List<Week> weeks = new ArrayList<>();
      String[] weekBlocks = zoneBlock.split("WEEK:");

      for (int i = 1; i < weekBlocks.length; i++) {
        Week week = parseWeek(weekBlocks[i]);
        if (week != null) {
          weeks.add(week);
        }
      }

      zone.setWeeks(weeks);
      zones.add(zone);
    }

    if (zones.isEmpty()) {
      return createFallbackRoadmap(vacancy);
    }

    roadmap.setRoadmapZones(zones);
    return roadmap;
  }

  private Week parseWeek(String weekBlock) {
    try {
      Week week = new Week();
      String[] lines = weekBlock.split("\n");

      for (String line : lines) {
        if (line.startsWith("GOAL:")) {
          week.setGoal(line.substring(5).trim());
        } else if (line.startsWith("TASKS:")) {
          // Создаём задачи
        } else if (line.startsWith("URLS:")) {
          // Добавляем ссылки
        }
      }

      return week;
    } catch (Exception e) {
      return null;
    }
  }

  private Roadmap createFallbackRoadmap(String vacancy) {
    Roadmap roadmap = new Roadmap();
    List<RoadmapZone> zones = new ArrayList<>();

    // Создаём дефолтные зоны
    RoadmapZone zone1 = new RoadmapZone();
    zone1.setName("Основы и введение");
    zone1.setComplexityLevel("Начальный");
    zone1.setLearningGoal("Изучение фундаментальных концепций " + vacancy);
    zones.add(zone1);

    RoadmapZone zone2 = new RoadmapZone();
    zone2.setName("Практика и применение");
    zone2.setComplexityLevel("Средний");
    zone2.setLearningGoal("Разработка практических навыков для " + vacancy);
    zones.add(zone2);

    RoadmapZone zone3 = new RoadmapZone();
    zone3.setName("Проекты и портфолио");
    zone3.setComplexityLevel("Продвинутый");
    zone3.setLearningGoal("Создание проектов для портфолио");
    zones.add(zone3);

    roadmap.setRoadmapZones(zones);
    return roadmap;
  }
}
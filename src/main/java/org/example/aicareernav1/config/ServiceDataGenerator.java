package org.example.aicareernav1.config;

import java.util.logging.Logger;
import org.example.aicareernav1.model.courseModel.Task;
import org.example.aicareernav1.model.courseModel.Week;
import org.example.aicareernav1.model.roadmap.Roadmap;
import org.example.aicareernav1.model.roadmap.RoadmapZone;
import org.example.aicareernav1.model.user.entity.*;
import org.example.aicareernav1.service.roadmap.RoadmapService;
import org.example.aicareernav1.service.user.util.PasswordEncoder;
import org.example.aicareernav1.repository.user.jpa.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component

public class ServiceDataGenerator {

  Logger logger = Logger.getLogger(getClass().getName());

  private final UserJpaRepository userRepository;
  private final CVDataJpaRepository cvDataRepository;
  private final UserPreferencesJpaRepository userPreferencesRepository;
  private final UserSkillsJpaRepository userSkillsRepository;
  private final RoadmapService roadmapService;
  private final Random random = new Random();

  public ServiceDataGenerator(UserJpaRepository userRepository,
      CVDataJpaRepository cvDataRepository,
      UserPreferencesJpaRepository userPreferencesRepository,
      UserSkillsJpaRepository userSkillsRepository,
      RoadmapService roadmapService) {
    this.userRepository = userRepository;
    this.cvDataRepository = cvDataRepository;
    this.userPreferencesRepository = userPreferencesRepository;
    this.userSkillsRepository = userSkillsRepository;
    this.roadmapService = roadmapService;
  }

  public void generateAllTestData() {
    logger.info("🎲 Generating test data using services...");
    cleanupTestData();
    List<UserEntity> testUsers = generateUsers();
    for (UserEntity user : testUsers) {
      generateUserProfile(user);
    }
    logger.info("✅ Test data generated successfully");
  }

  private void cleanupTestData() {
    logger.info("🧹 Cleaning up TEST data only...");
    try {
      List<UserEntity> allUsers = userRepository.findAll();
      int deletedCount = 0;
      for (UserEntity user : allUsers) {
        if (isTestUser(user)) {
          userRepository.delete(user);
          deletedCount++;
          logger.info("🗑️ Deleted test user: " + user.getEmail());
        }
      }
      logger.info("✅ Deleted " + deletedCount + " test users");
    } catch (Exception e) {
      System.err.println("❌ Error during test data cleanup: " + e.getMessage());
    }
  }

  private boolean isTestUser(UserEntity user) {
    return user.getEmail().endsWith("@demo.com") || user.getEmail().equals("demo@aicareer.com");
  }

  private List<UserEntity> generateUsers() {
    List<UserEntity> users = new ArrayList<>();
    String[][] userData = {
        {"Алексей Демо", "alex@demo.com", "hash123WW", "Java Developer"},
        {"Мария Тестова", "maria@demo.com", "hash123WW", "Frontend Developer"},
        {"Иван Примеров", "ivan@demo.com", "hash123WW", "Fullstack Developer"},
        {"Демо Пользователь", "demo@aicareer.com", "demo123WW", "Backend Engineer"},
        {"Екатерина Смирнова", "ekaterina@demo.com", "hash123WW", "Data Scientist"},
        {"Дмитрий Петров", "dmitry@demo.com", "hash123WW", "DevOps Engineer"},
        {"Ольга Козлова", "olga@demo.com", "hash123WW", "Mobile Developer"},
        {"Сергей Иванов", "sergey@demo.com", "hash123WW", "QA Engineer"},
        {"Анна Сидорова", "anna@demo.com", "hash123WW", "Team Lead"},
        {"Павел Николаев", "pavel@demo.com", "hash123WW", "Software Architect"}
    };
    for (String[] data : userData) {
      try {
        UserEntity user = UserEntity.builder()
            .name(data[0])
            .email(data[1])
            .passwordHash(PasswordEncoder.encode(data[2]))
            .vacancyNow(data[3])
            .build();
        UserEntity savedUser = userRepository.save(user);
        users.add(savedUser);
        logger.info("👤 Created user: " + savedUser.getEmail());
      } catch (Exception e) {
        System.err.println("❌ Failed to create user " + data[1] + ": " + e.getMessage());
      }
    }
    return users;
  }

  private void generateUserProfile(UserEntity user) {
    generateCVData(user);
    generateUserPreferences(user);
    generateRoadmapForUser(user);
  }

  private void generateCVData(UserEntity user) {
    try {
      CVDataEntity cvData = CVDataEntity.builder()
          .user(user)
          .information(getCVInformation(user.getVacancyNow()))
          .build();
      cvDataRepository.save(cvData);
      logger.info("📝 Created CV data for user: " + user.getEmail());
    } catch (Exception e) {
      System.err.println("❌ Error creating CV data for " + user.getEmail() + ": " + e.getMessage());
    }
  }

  private void generateUserPreferences(UserEntity user) {
    try {
      UserPreferencesEntity preferences = UserPreferencesEntity.builder()
          .user(user)
          .infoAboutPerson(getUserPreferencesInfo(user.getVacancyNow()))
          .build();
      userPreferencesRepository.save(preferences);
      logger.info("⚙️ Created preferences for user: " + user.getEmail());
    } catch (Exception e) {
      System.err.println("❌ Error creating preferences for " + user.getEmail() + ": " + e.getMessage());
    }
  }

  private void generateRoadmapForUser(UserEntity user) {
    try {
      Roadmap roadmap = Roadmap.builder()
          .userId(user.getId())
          .build();
      List<RoadmapZone> zones = generateRoadmapZones();
      roadmap.setRoadmapZones(zones);
      Roadmap savedRoadmap = roadmapService.saveCompleteRoadmap(roadmap);
      user.setRoadmapId(savedRoadmap.getId());
      userRepository.save(user);
      logger.info("🗺️ Created roadmap for user: " + user.getEmail());
    } catch (Exception e) {
      System.err.println("❌ Error creating roadmap for " + user.getEmail() + ": " + e.getMessage());
    }
  }

  // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ГЕНЕРАЦИИ (скопированы из исходного файла) ==========

  private String getCVInformation(String vacancy) {
    Map<String, String> cvTemplates = new HashMap<>();
    cvTemplates.put("Java Developer",
        "Опытный Java-разработчик с 5+ лет опыта. Специализация: Spring Boot, микросервисы, PostgreSQL.");
    cvTemplates.put("Frontend Developer",
        "Frontend разработчик с глубокими знаниями React и TypeScript. Опыт работы в Agile-командах.");
    cvTemplates.put("Fullstack Developer",
        "Fullstack developer с опытом работы как на бэкенде (Java), так и на фронтенде (React).");
    cvTemplates.put("Backend Engineer",
        "Backend engineer с фокусом на создании масштабируемых API.");
    cvTemplates.put("Data Scientist",
        "Data Scientist с опытом в машинном обучении и анализе больших данных. Владение Python, Pandas, Scikit-learn.");
    cvTemplates.put("DevOps Engineer",
        "DevOps инженер с опытом настройки CI/CD, контейнеризации и облачных технологий.");
    cvTemplates.put("Mobile Developer",
        "Mobile разработчик с опытом создания приложений для iOS и Android на React Native.");
    cvTemplates.put("QA Engineer",
        "QA инженер с глубокими знаниями автоматизированного тестирования и процессов обеспечения качества.");
    cvTemplates.put("Team Lead",
        "Team Lead с опытом управления командами разработки и организации Agile-процессов.");
    cvTemplates.put("Software Architect",
        "Software Architect с экспертизой в проектировании масштабируемых систем и выборе технологических решений.");

    return cvTemplates.getOrDefault(vacancy, "Информация о профессиональном опыте и навыках.");
  }

  private String getUserPreferencesInfo(String vacancy) {
    Map<String, String> preferenceTemplates = new HashMap<>();
    preferenceTemplates.put("Java Developer",
        "Предпочитаю практический подход к обучению. Интересуюсь микросервисной архитектурой.");
    preferenceTemplates.put("Frontend Developer",
        "Нравится работать над UI/UX, уделяю внимание деталям. Хочу развиваться в направлении Team Lead.");
    preferenceTemplates.put("Fullstack Developer",
        "Ищу баланс между глубокими техническими знаниями и управленческими навыками.");
    preferenceTemplates.put("Backend Engineer",
        "Ценю чистый код и лучшие практики разработки.");
    preferenceTemplates.put("Data Scientist",
        "Увлекаюсь анализом данных и машинным обучением. Хочу углубиться в нейросети.");
    preferenceTemplates.put("DevOps Engineer",
        "Интересуюсь автоматизацией процессов и облачными технологиями. Предпочитаю hands-on подход.");
    preferenceTemplates.put("Mobile Developer",
        "Люблю создавать удобные мобильные интерфейсы. Интересуюсь кросс-платформенной разработкой.");
    preferenceTemplates.put("QA Engineer",
        "Внимателен к деталям, ценю качество кода. Хочу развивать навыки автоматизации тестирования.");
    preferenceTemplates.put("Team Lead",
        "Стремлюсь к развитию лидерских качеств и управленческих навыков вместе с технической экспертизой.");
    preferenceTemplates.put("Software Architect",
        "Интересуюсь системным проектированием и выбором оптимальных архитектурных решений.");

    return preferenceTemplates.getOrDefault(vacancy, "Информация о предпочтениях в обучении.");
  }

  private List<RoadmapZone> generateRoadmapZones() {
    List<RoadmapZone> zones = new ArrayList<>();
    String[][] zoneData = {
        {"Основы программирования", "Изучение базовых концепций", "BEGINNER"},
        {"Фреймворки и инструменты", "Освоение популярных фреймворков", "INTERMEDIATE"},
        {"Продвинутые темы", "Углубленное изучение архитектуры", "ADVANCED"}
    };

    for (int i = 0; i < zoneData.length; i++) {
      RoadmapZone zone = RoadmapZone.builder()
          .name(zoneData[i][0])
          .learningGoal(zoneData[i][1])
          .complexityLevel(zoneData[i][2])
          .zoneOrder(i + 1)
          .weeks(generateWeeksForZone(zoneData[i][0], i + 1))
          .build();
      zone.updateTimestamps();
      zones.add(zone);
    }
    return zones;
  }

  private List<Week> generateWeeksForZone(String zoneName, int zoneOrder) {
    List<Week> weeks = new ArrayList<>();

    // Цели для разных зон
    Map<String, String[]> weekGoals = new HashMap<>();

    weekGoals.put("Основы программирования", new String[]{
        "Основы синтаксиса и структуры программы - Изучение базового синтаксиса, переменных, типов данных",
        "Управляющие конструкции и функции - Освоение условий, циклов, функций и основ отладки",
        "Объектно-ориентированное программирование - Изучение классов, объектов, наследования",
        "Работа с исключениями и коллекциями - Обработка ошибок и основные структуры данных",
        "Введение в алгоритмы - Базовые алгоритмы сортировки и поиска",
        "Решение задач - Комплексные задачи для закрепления материала"
    });

    weekGoals.put("Фреймворки и инструменты", new String[]{
        "Знакомство с фреймворком и его экосистемой - Установка и настройка окружения",
        "Практическое применение фреймворка - Создание первого приложения",
        "Работа с базами данных - Интеграция с БД, ORM, миграции",
        "Аутентификация и авторизация - Реализация системы безопасности",
        "Тестирование приложения - Unit и интеграционные тесты",
        "Деплой и мониторинг - Развертывание приложения, Docker"
    });

    weekGoals.put("Продвинутые темы", new String[]{
        "Архитектурные паттерны и лучшие практики - Изучение продвинутых архитектурных решений",
        "Принципы SOLID и DRY - Применение принципов в реальных проектах",
        "Микросервисная архитектура - Основы распределенных систем",
        "Оптимизация производительности - Профилирование и оптимизация запросов",
        "Код-ревью и лучшие практики - Проведение код-ревью, code style",
        "Подготовка к реальным проектам - Решение сложных задач, работа в команде"
    });

    String[] goals = weekGoals.getOrDefault(zoneName, new String[]{
        "Изучение основных концепций - Освоение фундаментальных принципов",
        "Практическое применение - Закрепление знаний на практике",
        "Углубленное изучение - Детальный разбор темы",
        "Практикум - Решение практических задач",
        "Проектная работа - Разработка проекта",
        "Закрепление материала - Повторение и систематизация знаний"
    });

    for (int i = 0; i < 6; i++) {
      int weekNum = i + 1;
      String goal = goals[i % goals.length];

      Week week = Week.builder()
          .number(weekNum)
          .goal(goal)
          .tasks(generateTasksForWeek(zoneName, weekNum, zoneOrder))
          .build();
      week.updateTimestamps();
      weeks.add(week);
    }
    return weeks;
  }

  private List<Task> generateTasksForWeek(String zoneName, int weekNumber, int zoneOrder) {
    List<Task> tasks = new ArrayList<>();

    Map<String, List<String[]>> taskData = new HashMap<>();

    taskData.put("Основы программирования_1", Arrays.asList(
        new String[]{
            "Изучение синтаксиса и типов данных",
            "Пройдите уроки по базовому синтаксису, объявлению переменных и работе с примитивными типами данных",
            "Книга «Java: Основы» автор К. С. Хорстманн",
            "Видео «Java для начинающих» на YouTube канал «JavaMaster»"
        },
        new String[]{
            "Практика с операторами",
            "Решите 10-15 задач на арифметические и логические операторы",
            "Практикум «Java Basics» на Stepik.org",
            "Статья «Операторы в Java» на Хабр.ру"
        }
    ));

    taskData.put("Основы программирования_2", Arrays.asList(
        new String[]{
            "Условия и ветвления",
            "Изучите конструкции if-else, switch-case и решите практические задачи",
            "Книга «Java: Основы» главы 3-4",
            "Видео «Условные операторы» на YouTube"
        },
        new String[]{
            "Циклы и итерации",
            "Освойте циклы for, while, do-while. Решите задачи на обработку последовательностей",
            "Практикум на CodeWars",
            "Статья «Циклы в программировании»"
        }
    ));

    taskData.put("Фреймворки и инструменты_1", Arrays.asList(
        new String[]{
            "Установка и настройка окружения",
            "Установите фреймворк, настройте IDE и создайте базовый проект",
            "Официальная документация Spring Boot",
            "Видео «Настройка Spring Boot» на YouTube"
        },
        new String[]{
            "Изучение базовой архитектуры",
            "Пройдите туториал по основным компонентам и их взаимодействию",
            "Книга «Spring в действии» главы 1-2",
            "Статья «Архитектура Spring» на Medium"
        }
    ));

    taskData.put("Фреймворки и инструменты_2", Arrays.asList(
        new String[]{
            "Работа с основными модулями",
            "Изучите ключевые модули фреймворка на практических примерах",
            "Курс «Spring Core» на Udemy",
            "Видео «Spring Modules» на Rutube"
        },
        new String[]{
            "Интеграция с базами данных",
            "Настройте подключение к БД и реализуйте CRUD операции",
            "Документация Spring Data JPA",
            "Статья «Spring Boot + PostgreSQL» на Хабр.ру"
        }
    ));

    taskData.put("Продвинутые темы_1", Arrays.asList(
        new String[]{
            "Изучение архитектурных паттернов",
            "Разберите MVC, MVVM, Clean Architecture на практических кейсах",
            "Книга «Чистая архитектура» автор Роберт Мартин",
            "Видео «Архитектурные паттерны» на YouTube"
        },
        new String[]{
            "Принципы SOLID и DRY",
            "Проанализируйте код на соответствие принципам и выполните рефакторинг",
            "Статья «SOLID принципы» на Medium",
            "Курс «Clean Code» на Coursera"
        }
    ));

    taskData.put("Продвинутые темы_2", Arrays.asList(
        new String[]{
            "Решение комплексной задачи",
            "Разработайте полноценное приложение с использованием изученных паттернов",
            "Книга «Паттерны проектирования»",
            "Видео «Проектирование систем» на Rutube"
        },
        new String[]{
            "Код-ревью и рефакторинг",
            "Проведите анализ чужого кода и предложите улучшения",
            "Статья «Лучшие практики код-ревью» на Хабр.ру",
            "Видео «Искусство рефакторинга»"
        }
    ));

    String key = zoneName + "_" + weekNumber;
    List<String[]> tasksForWeek = taskData.get(key);

    if (tasksForWeek == null) {
      String[][] generalTemplates = {
          {
              "Теоретическое изучение материалов",
              "Освоение базовых концепций темы через изучение литературы и видео материалов",
              "Книга «Основы темы» автор С. И. Петров (главы " + (weekNumber * 2 - 1) + "-" + (weekNumber * 2) + ")",
              "Видео «Лекция по теме " + weekNumber + "» на Rutube канал «Образование»"
          },
          {
              "Практическое задание",
              "Применение изученных концепций на практике через решение задач и упражнений",
              "Статья «Практические примеры» на Хабр.ru",
              "Курс «Практикум» на Stepik.org модуль " + weekNumber
          },
          {
              "Проектная работа",
              "Разработка небольшого проекта для закрепления навыков",
              "Проект «Пример реализации» на GitHub",
              "Видео «Разбор проекта» на YouTube"
          }
      };
      tasksForWeek = Arrays.asList(generalTemplates);
    }

    for (int i = 0; i < Math.min(tasksForWeek.size(), 3); i++) {
      String[] taskInfo = tasksForWeek.get(i);

      List<String> resources = new ArrayList<>();
      resources.add("RESOURSES 1: " + taskInfo[2]);
      resources.add("RESOURSES 2: " + taskInfo[3]);

      if (random.nextBoolean() && i == 0) {
        String[] extraResources = {
            "Онлайн-курс «Продвинутые техники» на платформе Coursera",
            "Вебинар «Современные подходы» от компании Яндекс",
            "Документация на официальном сайте технологии",
            "Форум «Вопросы и ответы» для разработчиков"
        };
        resources.add("RESOURSES 3: " + extraResources[random.nextInt(extraResources.length)]);
      }

      Task task = Task.builder()
          .description(taskInfo[0] + ". " + taskInfo[1])
          .urls(resources)
          .build();
      task.updateTimestamps();
      tasks.add(task);
    }
    return tasks;
  }
}
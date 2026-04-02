package org.example.aicareernav1.service.testService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.testDto.QuestionDto;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizService {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;

  public List<String> getQuestionsFromSite(String vacancy) {
    List<String> questionsList = new ArrayList<>();

    String url = buildUrlFromDatabaseString(vacancy);

    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.addArguments("--headless");
    chromeOptions.addArguments("--disable-gpu");
    chromeOptions.addArguments("--window-size=1920,1080");

    System.setProperty("webdriver.chrome.silentOutput", "true");
    ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);

    try {
      log.info("🤖 Selenium открывает страницу: {}", url);
      chromeDriver.get(url);

      log.info("⏳ Ждем 5 секунд, пока прогрузится JS...");
      Thread.sleep(5000);

      String pageSource = chromeDriver.getPageSource();

      log.info("📄 Длина скачанной страницы: {} символов.", pageSource.length());

      Document document = Jsoup.parse(pageSource);

      Elements questions = document.select("article h2.text-gray-800");
      for (Element q : questions) {
        String questionText = q.text().trim();
        if (questionText.equalsIgnoreCase("Есть ли вопрос?")) {
          log.info("🚫 Пропущен технический заголовок: {}", questionText);
          continue;
        }
        if (questionText.isEmpty()) {
          continue;
        }

        questionsList.add(questionText);
      }

    } catch (InterruptedException e) {
      log.error("Поток прерван!", e);
      Thread.currentThread().interrupt();
    } finally {
      log.info("🛑 Закрываем фоновый браузер.");
      chromeDriver.quit();
    }

    return questionsList;
  }

  public List<QuestionDto> generateAndSaveQuestions(Long userId, String vacancy) throws JsonProcessingException {
    List<String> parsedQuestions = getQuestionsFromSite(vacancy);

    if (parsedQuestions.isEmpty()) {
      throw new RuntimeException("Не удалось спарсить вопросы с сайта.");
    }

    Collections.shuffle(parsedQuestions);

    int limit = Math.min(12, parsedQuestions.size());
    List<String> selectedQuestions = parsedQuestions.subList(0, limit);

    List<QuestionDto> dtoList = new ArrayList<>();
    for (int i = 0; i < selectedQuestions.size(); i++) {
      QuestionDto dto = new QuestionDto();
      dto.setNumber(i + 1);
      dto.setQuestion(selectedQuestions.get(i));
      dtoList.add(dto);
    }

    String redisKey = "quiz:user:" + userId;
    String jsonQuestions = objectMapper.writeValueAsString(dtoList);
    redisTemplate.opsForValue().set(redisKey, jsonQuestions, 1, TimeUnit.HOURS);

    log.info("Для пользователя {} успешно сохранено {} вопросов в Redis", userId, dtoList.size());

    return dtoList;
  }

  public void createQuizSession(Long userId) {
    String key = "quiz:answers:" + userId;
    redisTemplate.opsForValue().set(key, "{}", 1, TimeUnit.HOURS);
    log.info("Создана сессия ответов для пользователя {}", userId);
  }

  public List<QuestionDto> getQuestions(Long userId) {
    String redisKey = "quiz:user:" + userId;
    String json = redisTemplate.opsForValue().get(redisKey);

    if (json == null || json.isEmpty()) {
      return Collections.emptyList();
    }

    try {
      return objectMapper.readValue(json,
        objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionDto.class));
    } catch (JsonProcessingException e) {
      log.error("Ошибка десериализации вопросов из Redis", e);
      return Collections.emptyList();
    }
  }

  public void saveAnswer(Long userId, String questionText, String answer) {
    UserEntity user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    Map<String, String> answersMap = new java.util.HashMap<>();
    String currentTestResult = user.getTestResult();

    try {
      if (currentTestResult != null && !currentTestResult.trim().isEmpty() && !currentTestResult.equals("{}")) {
        answersMap = objectMapper.readValue(currentTestResult, Map.class);
      }
      answersMap.put(questionText, answer);
      String updatedTestResult = objectMapper.writeValueAsString(answersMap);
      user.setTestResult(updatedTestResult);
      userRepository.save(user);
      log.info("Ответ сохранен в поле test_result для пользователя {}", userId);

    } catch (JsonProcessingException e) {
      log.error("Ошибка при сериализации/десериализации test_result для юзера {}", userId, e);
    }
  }

  public Map<String, String> getAllAnswers(Long userId) {
    UserEntity user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    String currentTestResult = user.getTestResult();

    if (currentTestResult == null || currentTestResult.trim().isEmpty() || currentTestResult.equals("{}")) {
      return Collections.emptyMap();
    }

    try {
      return objectMapper.readValue(currentTestResult, Map.class);
    } catch (JsonProcessingException e) {
      log.error("Ошибка при чтении test_result для юзера {}", userId, e);
      return Collections.emptyMap();
    }
  }

  private String buildUrlFromDatabaseString(String vacancyFromDb) {
    if (vacancyFromDb == null || vacancyFromDb.trim().isEmpty()) {
      throw new IllegalArgumentException("Вакансия в базе данных пуста!");
    }

    String[] words = vacancyFromDb.trim().toLowerCase().split("\\s+");

    String techName;
    String grade = "all";
    String firstWord = words[0];

    if (firstWord.equals("senior") || firstWord.equals("middle") || firstWord.equals("junior")) {
      grade = firstWord;
      if (words.length > 1) {
        techName = words[1];
      } else {
        techName = "java";
      }
    } else {
      techName = firstWord;
    }

    String url = "https://easyoffer.ru/" + techName + "-developer/questions";

    if (!grade.equals("all")) {
      url = url + "/" + grade;
    }

    return url;
  }
}
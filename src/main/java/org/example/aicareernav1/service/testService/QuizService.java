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

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizService {

  private static final String BASE_URL = "https://easyoffer.ru/";
  private static final String REDIS_QUIZ_PREFIX = "quiz:user:";
  private static final String REDIS_ANSWERS_PREFIX = "quiz:answers:";

  private static final Map<String, String> VACANCY_URLS_MAPPING;
  static {
    Map<String, String> map = new HashMap<>();
    map.put("qa тестировщик", "qa-testirovshik");
    map.put("aqa / automation", "qa-automation");
    map.put("data science", "data-scientist");
    map.put("бизнес аналитик", "business-analyst");
    map.put("системный аналитик", "system-analyst");
    map.put("аналитик данных", "data-analyst");
    map.put("продуктовый аналитик", "product-analyst");
    map.put("менеджер проектов", "it-project-manager");
    map.put("продукт менеджер", "it-product-manger");
    map.put("1с программист", "1c-developer");
    map.put("ios / swift", "ios-developer");
    map.put("c#", "c-sharp-developer");
    VACANCY_URLS_MAPPING = Collections.unmodifiableMap(map);
  }

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;

  public List<String> getQuestionsFromSite(String vacancy) {
    String url = buildUrlFromDatabaseString(vacancy);
    ChromeDriver chromeDriver = createSilentHeadlessDriver();

    try {
      log.info("🤖 Selenium открывает страницу: {}", url);
      chromeDriver.get(url);

      log.info("⏳ Ждем 5 секунд, пока прогрузится JS...");
      Thread.sleep(5000);

      String pageSource = chromeDriver.getPageSource();
      log.info("📄 Длина скачанной страницы: {} символов.", pageSource.length());

      return extractAndFilterQuestions(pageSource);

    } catch (InterruptedException e) {
      log.error("Поток прерван!", e);
      Thread.currentThread().interrupt();
      return Collections.emptyList();
    } finally {
      log.info("🛑 Закрываем фоновый браузер.");
      chromeDriver.quit();
    }
  }

  public List<QuestionDto> generateAndSaveQuestions(Long userId, String vacancy) throws JsonProcessingException {
    List<String> parsedQuestions = getQuestionsFromSite(vacancy);

    if (parsedQuestions.isEmpty()) {
      throw new RuntimeException("Не удалось спарсить вопросы с сайта.");
    }

    Collections.shuffle(parsedQuestions);

    List<QuestionDto> dtoList = createLimitedQuestionDtoList(parsedQuestions, 12);

    String redisKey = REDIS_QUIZ_PREFIX + userId;
    String jsonQuestions = objectMapper.writeValueAsString(dtoList);
    redisTemplate.opsForValue().set(redisKey, jsonQuestions, 1, TimeUnit.HOURS);

    log.info("Для пользователя {} успешно сохранено {} вопросов в Redis", userId, dtoList.size());
    return dtoList;
  }

  public void createQuizSession(Long userId) {
    String key = REDIS_ANSWERS_PREFIX + userId;
    redisTemplate.opsForValue().set(key, "{}", 1, TimeUnit.HOURS);
    log.info("Создана сессия ответов для пользователя {}", userId);
  }

  public List<QuestionDto> getQuestions(Long userId) {
    String redisKey = REDIS_QUIZ_PREFIX + userId;
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
    UserEntity user = findUserById(userId);
    String currentTestResult = user.getTestResult();

    try {
      Map<String, String> answersMap = deserializeAnswers(currentTestResult);
      answersMap.put(questionText, answer);

      user.setTestResult(objectMapper.writeValueAsString(answersMap));
      userRepository.save(user);
      log.info("Ответ сохранен в поле test_result для пользователя {}", userId);

    } catch (JsonProcessingException e) {
      log.error("Ошибка при сериализации/десериализации test_result для юзера {}", userId, e);
    }
  }

  public Map<String, String> getAllAnswers(Long userId) {
    UserEntity user = findUserById(userId);
    String currentTestResult = user.getTestResult();

    if (currentTestResult == null || currentTestResult.trim().isEmpty() || "{}".equals(currentTestResult)) {
      return Collections.emptyMap();
    }

    try {
      return objectMapper.readValue(currentTestResult, Map.class);
    } catch (JsonProcessingException e) {
      log.error("Ошибка при чтении test_result для юзера {}", userId, e);
      return Collections.emptyMap();
    }
  }

  private ChromeDriver createSilentHeadlessDriver() {
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.addArguments("--headless");
    chromeOptions.addArguments("--disable-gpu");
    chromeOptions.addArguments("--window-size=1920,1080");

    System.setProperty("webdriver.chrome.silentOutput", "true");
    return new ChromeDriver(chromeOptions);
  }

  private List<String> extractAndFilterQuestions(String pageSource) {
    List<String> questionsList = new ArrayList<>();
    Document document = Jsoup.parse(pageSource);
    Elements questions = document.select("article h2.text-gray-800");

    for (Element q : questions) {
      String questionText = q.text().trim();

      if (!questionText.isEmpty() && !containsInvalidKeyword(questionText)) {
        questionsList.add(questionText);
      }
    }
    return questionsList;
  }

  private boolean containsInvalidKeyword(String questionText) {
    String[] wordsInQuestion = questionText.toLowerCase().split("\\s+");
    for (String word : wordsInQuestion) {
      if (word.contains("вопрос")) {
        return true;
      }
    }
    return false;
  }

  private List<QuestionDto> createLimitedQuestionDtoList(List<String> parsedQuestions, int maxLimit) {
    int limit = Math.min(maxLimit, parsedQuestions.size());
    List<String> selectedQuestions = parsedQuestions.subList(0, limit);

    List<QuestionDto> dtoList = new ArrayList<>();
    for (int i = 0; i < selectedQuestions.size(); i++) {
      QuestionDto dto = new QuestionDto();
      dto.setNumber(i + 1);
      dto.setQuestion(selectedQuestions.get(i));
      dtoList.add(dto);
    }
    return dtoList;
  }

  private UserEntity findUserById(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
  }

  private Map<String, String> deserializeAnswers(String currentTestResult) throws JsonProcessingException {
    if (currentTestResult != null && !currentTestResult.trim().isEmpty() && !"{}".equals(currentTestResult)) {
      return objectMapper.readValue(currentTestResult, Map.class);
    }
    return new java.util.HashMap<>();
  }

  private String buildUrlFromDatabaseString(String vacancyFromDb) {
    if (vacancyFromDb == null || vacancyFromDb.trim().isEmpty()) {
      throw new IllegalArgumentException("Вакансия в базе данных пуста!");
    }

    String cleaned = vacancyFromDb.trim().toLowerCase();
    String grade = extractGrade(cleaned);
    String profession = extractProfession(cleaned, grade);

    String easyOfferPath = getEasyOfferPath(profession, cleaned);
    String url = BASE_URL + easyOfferPath + "/questions";

    if (!"all".equals(grade) && !VACANCY_URLS_MAPPING.containsKey(profession)) {
      url = url + "/" + grade;
    }

    log.info("🎯 Сформирован точный URL для EasyOffer: {}", url);
    return url;
  }

  private String extractGrade(String cleaned) {
    String firstWord = cleaned.split("\\s+")[0];
    if (firstWord.equals("senior") || firstWord.equals("middle") || firstWord.equals("junior")) {
      return firstWord;
    }
    return "all";
  }

  private String extractProfession(String cleaned, String grade) {
    if (!"all".equals(grade)) {
      return cleaned.substring(grade.length()).trim();
    }
    return cleaned;
  }

  private String getEasyOfferPath(String profession, String cleaned) {
    if (VACANCY_URLS_MAPPING.containsKey(profession)) {
      return VACANCY_URLS_MAPPING.get(profession);
    }

    String techName = profession.split("\\s+")[0];
    if (techName.contains("c++") || techName.contains("c/c++")) {
      techName = "c-plus";
    }

    String suffix = cleaned.contains("engineer") ? "engineer" : "developer";
    return techName + "-" + suffix;
  }
}
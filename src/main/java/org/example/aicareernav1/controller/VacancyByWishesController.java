package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.wishes.WishesResponseDto;
import org.example.aicareernav1.service.gigachat.GigaChatService;
import org.example.aicareernav1.service.wishes.WishesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyByWishesController {

  private final WishesService wishesService;
  private final GigaChatService gigaChatService;

  private static final String PROMPT = """
        На основе следующих пожеланий пользователя подбери 3 наиболее подходящие IT-вакансии.
        
        Пожелания пользователя:
        %s
        
        Требования:
        1. Вакансии должны быть реальными IT-профессиями
        2. Ответ должен быть строго в формате: Вакансия1, Вакансия2, Вакансия3
        3. Только названия вакансий через запятую, без дополнительного текста
        4. Названия должны быть на английском языке
        
        Пример: Java Developer, Python Developer, Frontend Developer
        """;

  @GetMapping("/by-wishes")
  public ResponseEntity<List<String>> getVacanciesByWishes(@RequestParam Long userId, HttpSession session) {
    try {
      // 1. Получаем пожелания пользователя
      WishesResponseDto wishesDto = wishesService.getWishesByUserId(userId);
      String wishes = wishesDto.getWishesMessage();

      log.info("Получены пожелания для пользователя {}: {}", userId, wishes);

      if (wishes == null || wishes.trim().isEmpty()) {
        log.warn("Пожелания пользователя {} пусты, возвращаем дефолтные вакансии", userId);
        List<String> defaultVacancies = getDefaultVacancies();
        session.setAttribute("suggestedVacancies", defaultVacancies);
        return ResponseEntity.ok(defaultVacancies);
      }

      // 2. Отправляем запрос в GigaChat
      String prompt = String.format(PROMPT, wishes);
      log.info("Отправка запроса в GigaChat для подбора вакансий");
      String aiResponse = gigaChatService.sendMessage(prompt);
      log.info("Ответ от GigaChat: {}", aiResponse);

      // 3. Парсим ответ
      List<String> vacancies = parseVacancies(aiResponse);

      // 4. Сохраняем в сессию
      session.setAttribute("suggestedVacancies", vacancies);

      log.info("Подобраны вакансии для пользователя {}: {}", userId, vacancies);

      return ResponseEntity.ok(vacancies);

    } catch (Exception e) {
      log.error("Ошибка при подборе вакансий", e);
      List<String> defaultVacancies = getDefaultVacancies();
      session.setAttribute("suggestedVacancies", defaultVacancies);
      return ResponseEntity.ok(defaultVacancies);
    }
  }

  private List<String> parseVacancies(String aiResponse) {
    if (aiResponse == null || aiResponse.trim().isEmpty()) {
      return getDefaultVacancies();
    }

    // Ищем строку с запятыми
    String[] lines = aiResponse.split("\n");
    for (String line : lines) {
      if (line.contains(",")) {
        String[] parts = line.split(",");
        List<String> vacancies = Arrays.stream(parts)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .limit(3)
            .toList();
        if (vacancies.size() >= 3) {
          return vacancies;
        }
      }
    }

    // Если не нашли, пробуем весь текст
    if (aiResponse.contains(",")) {
      String[] parts = aiResponse.split(",");
      List<String> vacancies = Arrays.stream(parts)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .limit(3)
          .toList();
      if (!vacancies.isEmpty()) {
        return vacancies;
      }
    }

    return getDefaultVacancies();
  }

  private List<String> getDefaultVacancies() {
    return Arrays.asList("Java Developer", "Python Developer", "Frontend Developer");
  }
}
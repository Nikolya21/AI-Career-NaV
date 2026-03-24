package com.aicareer.core.service.parser;

import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.vacancy.PotentialVacancy;
import com.aicareer.core.model.vacancy.RealVacancy;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.repository.SelectOfVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.parser.ParserService;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SelectVacancy implements SelectOfVacancy {

  private final List<String> listOfThreeVacancy = new ArrayList<>();
  private SelectedPotentialVacancy selectedVacancy;
  private String analysisResult;


  private final ParserService parserService;
  private final GigaChatService gigaChatService;

  public SelectVacancy(ParserService parserService, GigaChatService gigaChatService) {
    this.parserService = parserService;
    this.gigaChatService = gigaChatService;
  }

  @Override
  public String analyzeUserPreference(UserPreferences infoAboutPerson) {
    String promptAnalyze = (infoAboutPerson.toString() + "%s\n\nРоль: Ты — опытный HR-аналитик и карьерный психолог. Твоя задача — проанализировать диалог с пользователем, составить его детальный психологический портрет и на его основе подобрать три наиболее подходящие профессии.\n"
        + "\nКонтекст:\n"
        + "Пользователь прошел сессию карьерного консультирования. Тебе нужно глубоко проанализировать его ответы, чтобы понять его истинные мотивы, предпочтения и на основе этого предложить конкретные вакансии.\n"
        + "\nЗадача: Проведи комплексный анализ, а затем подбери профессии.\n"
        + "\nФормат вывода:\n"
        + "ПСИХОЛОГИЧЕСКИЙ ПОРТРЕТ ДЛЯ ПОДБОРА ВАКАНСИЙ\n"
        + "[анализ...]\n"
        + "ПОДБОР ПРОФЕССИЙ\n"
        + "[профессии...]\n"
        + ":::Профессия1,Профессия2,Профессия3\n"
        + "последняя строчка является обязательной и самым главным условием, так же учитывай что названия профессия должны быть на английском языке обязательно\n"
        + "ПРИМЕР: Javamiddle - \n"
        + "QA tester - \n"
        + "TeamLead -  \n"
        + "::: Javamiddle,Qa tester,TeamLead \n"
        + "после этих трех профессий ты должен закончить ответ и не добавлять больше символов!!!");
    String gigachatAnswer = gigaChatService.sendMessage(promptAnalyze);
    this.analysisResult = gigachatAnswer;
    extractThreeVacancies(gigachatAnswer, 0);
    return gigachatAnswer;
  }

  @Override
  public List<String> extractThreeVacancies(String gigachatAnswer, int count) {
    if (gigachatAnswer.contains(":::")) {
      String[] parts = gigachatAnswer.split(":::");
      if (parts.length < 3 && count < 5) {
        gigachatAnswer = validateAndFixResponse(gigachatAnswer);
      }
      if (parts.length > 1) {
        String vacanciesPart = parts[1].trim();
        String[] vacanciesArray = vacanciesPart.split(",");
        listOfThreeVacancy.clear();
        for (String vacancy : vacanciesArray) {
          listOfThreeVacancy.add(vacancy.trim());
        }

        while (listOfThreeVacancy.size() < 3) {
          listOfThreeVacancy.add("Вакансия " + (listOfThreeVacancy.size() + 1));
        }
      } else {
        String validateString = validateAndFixResponse(gigachatAnswer);
        extractThreeVacancies(validateString, ++count);
      }
    } else {
      gigachatAnswer = validateAndFixResponse(gigachatAnswer);
      extractThreeVacancies(gigachatAnswer, ++count);
    }
    return listOfThreeVacancy;
  }

  public String validateAndFixResponse(String rawResponse) {
    String simplified = preprocessResponse(rawResponse);
    if (isValidFormat(simplified)) {
      return simplified;
    }

    String validationPrompt = """
                КРИТИЧЕСКАЯ ЗАДАЧА: Исправь формат данных строго по шаблону

                ТВОЯ РОЛЬ: Форматировщик данных
                ЦЕЛЬ: Привести данные к строгому шаблону

                ШАБЛОН (обязателен):
                ":::Профессия1,Профессия2,Профессия3"

                ПРАВИЛА:
                - Начало: ровно 3 двоеточия ":::"
                - Затем 3 профессии через запятую
                - Без переносов строк
                - Без лишних символов
                - Только латиница/кириллица, запятые и пробелы между словами

                ДАННЫЕ ДЛЯ ИСПРАВЛЕНИЯ:
                %s

                КРИТИЧЕСКИЕ ТРЕБОВАНИЯ:
                1. Если профессий > 3 - оставь первые 3
                2. Если профессий < 3 - дополни до 3 (используй логику контекста)
                3. Удали все лишние символы, кроме букв, запятых и пробелов в названиях
                4. Убери все переносы строк
                5. Убедись в начале ровно ":::"

                ВЕРНИ ТОЛЬКО ИСПРАВЛЕННУЮ СТРОКУ БЕЗ ОБЪЯСНЕНИЙ!
                Пример: ":::Data Scientist,Business Analyst,Analytics Manager"
                """;

    return gigaChatService.sendMessage(String.format(validationPrompt, rawResponse));
  }

  private String preprocessResponse(String raw) {
    if (raw == null) return ":::Программист,Аналитик,Менеджер";

    String cleaned = raw.replace("\n", "").replace("\r", "").trim();

    if (cleaned.startsWith("::") && !cleaned.startsWith(":::")) {
      cleaned = ":::" + cleaned.substring(2);
    } else if (cleaned.startsWith(":") && !cleaned.startsWith(":::")) {
      cleaned = ":::" + cleaned.substring(1);
    } else if (!cleaned.startsWith(":::")) {
      cleaned = ":::" + cleaned;
    }

    return cleaned;
  }

  private boolean isValidFormat(String response) {
    return response != null &&
        response.startsWith(":::") &&
        response.length() > 3 &&
        countCommas(response) >= 2;
  }

  private int countCommas(String str) {
    int count = 0;
    for (char c : str.toCharArray()) {
      if (c == ',') count++;
    }
    return count;
  }

  @Override
  public SelectedPotentialVacancy choosenVacansy(List<String> listOfThreeVacancy) {
    System.out.println("Выберите одну из трех предложенных вакансий:");
    for (int i = 0; i < listOfThreeVacancy.size(); i++) {
      System.out.println((i + 1) + ". " + listOfThreeVacancy.get(i));
    }

    Scanner scan = new Scanner(System.in);
    int chosenNumber = scan.nextInt();
    scan.nextLine();

    if (chosenNumber > 0 && chosenNumber <= listOfThreeVacancy.size()) {
      String chosenVacancyName = listOfThreeVacancy.get(chosenNumber - 1);

      PotentialVacancy potentialVacancy = new PotentialVacancy();
      potentialVacancy.setNameOfVacancy(chosenVacancyName);

      this.selectedVacancy = new SelectedPotentialVacancy(potentialVacancy);
    }

    System.out.println("Выбрана вакансия: " +
        (selectedVacancy != null ? selectedVacancy.getNameOfVacancy() : "не выбрана"));

    return selectedVacancy;
  }

  @Override
  public String formingByParsing(SelectedPotentialVacancy selectedVacancy) {
    String selectedVacancyName = this.selectedVacancy.getNameOfVacancy();
    List<RealVacancy> vacancies = parserService.getVacancies(selectedVacancyName, "1", 50);
    AtomicReference<String> newPrompt = new AtomicReference<>("");
    vacancies.forEach(v -> {
      if (!v.getVacancyRequirements().isEmpty()) {
        System.out.println(v.getNameOfVacancy() + "   " + v.getVacancyRequirements());
      } else {
        String answer = gigaChatService.sendMessage(
            "Ниже тебе будет дано описании вакансии, найди и вычлени только ключевые навыки связанные с айти и программированием напрямую, которые требуют : \n"
                + v.getDescription()
                + "в ответ дай только 5-10 основных навыков в формате [Python, SQL, Linux, OpenOffice, Java Spring] обязательно обернув в квадрантые скобочки и не добавляя кавычек, а также в одну строку");

        newPrompt.set(answer);
      }
    });
    return newPrompt.get();
  }

  @Override
  public FinalVacancyRequirements formingFinalVacancyRequirements(String newPrompt) {
    String gigachatFinalRequirements = gigaChatService.sendMessage(newPrompt);
    return new FinalVacancyRequirements(gigachatFinalRequirements);
  }

  public SelectedPotentialVacancy chooseVacancyManually(List<String> listOfThreeVacancy, String chosenVacancyName) {
    if (listOfThreeVacancy == null || !listOfThreeVacancy.contains(chosenVacancyName)) {
      return null;
    }

    PotentialVacancy potentialVacancy = new PotentialVacancy();
    potentialVacancy.setNameOfVacancy(chosenVacancyName);

    this.selectedVacancy = new SelectedPotentialVacancy(potentialVacancy);

    System.out.println("Выбрана вакансия: " +
        (selectedVacancy != null ? selectedVacancy.getNameOfVacancy() : "не выбрана"));

    return selectedVacancy;
  }
}
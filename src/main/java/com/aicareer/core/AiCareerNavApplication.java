package com.aicareer.core;

import com.aicareer.core.model.vacancy.RealVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.parser.ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class AiCareerNavApplication implements CommandLineRunner {
  private final GigaChatService gigaChatService;
  private final ParserService parserService;

  public static void main(String[] args) {
    SpringApplication.run(AiCareerNavApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    System.out.println("=".repeat(50));
    System.out.println("🚀 НАЧАЛО ПРОВЕРКИ PARSER SERVICE");
    System.out.println("=".repeat(50));

    // Проверяем ParserService
    var vacancies = parserService.getVacancies("Java", "1", 5);

    System.out.println("\n📊 Результат:");
    System.out.println("Найдено вакансий: " + vacancies.size());

    if (vacancies.isEmpty()) {
      System.out.println("⚠️ Вакансии не найдены. Проверьте интернет и API hh.ru");
    } else {
      for (RealVacancy v : vacancies) {
        System.out.println("\n--- Вакансия ---");
        System.out.println("Название: " + v.getNameOfVacancy());
        System.out.println("Работодатель: " + v.getEmployer());
        System.out.println("Зарплата: " + v.getSalary());
        System.out.println("Опыт: " + v.getExperience());
        if (!v.getVacancyRequirements().isEmpty()) {
          for (int i = 0; i < v.getVacancyRequirements().size(); i++) {
            System.out.print(v.getVacancyRequirements().get(i) + " ");
          }}
          else{

            System.out.println(v.getNameOfVacancy() );
          }


      }
    }

    System.out.println("\n" + "=".repeat(50));
    System.out.println("✅ ПРОВЕРКА ЗАВЕРШЕНА");
    System.out.println("=".repeat(50));

    // Приложение продолжает работать, можно отправлять HTTP-запросы
    System.out.println("\n🌐 Сервер запущен. Откройте в браузере:");
    System.out.println("http://localhost:8080/api/v1/vacancies?searchText=Java&area=1&perPage=5");
  }
}
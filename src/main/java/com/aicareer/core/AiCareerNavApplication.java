package com.aicareer.core;

import com.aicareer.core.service.parser.ParserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AiCareerNavApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(AiCareerNavApplication.class, args);

    // Получаем бин ParserService
    ParserService parserService = context.getBean(ParserService.class);

    // Вызываем метод
    var vacancies = parserService.getVacancies("Java", "1", 5);
    vacancies.forEach(v -> System.out.println(v.getNameOfVacancy()));

    // Можно закрыть контекст (не обязательно, приложение завершится само)
  }
}
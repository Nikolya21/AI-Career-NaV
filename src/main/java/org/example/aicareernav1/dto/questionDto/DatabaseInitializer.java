package org.example.aicareernav1.dto.questionDto;

import lombok.AllArgsConstructor;
import org.example.aicareernav1.repository.ParsingSites;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.service.Scraper.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {
    private final QuestionRepository questionRepository;
    private final List<ParsingSites> scrapers;
    private final ItvdnService itvdnService;
    private final QuestionService questionService;
    private final PythonItvdnService pythonItvdnService;
    private final MachineLearningLabexService machineLearningLabexService;
    private final NoSqlHabrService noSqlHabrService;
    private final IosHabrService iosHabrService;
    // Внедряем ВСЕ зависимости через один конструктор (лучшая практика Spring)

    @Override
    public void run(String... args) throws Exception {

        // ПРОВЕРКА: Если в базе УЖЕ есть хоть один вопрос — выходим.
        if (questionRepository.count() > 0) {
            System.out.println(">>> База данных уже заполнена! Парсинг больше не требуется. <<<");
            return;
        }

        System.out.println(">>> База абсолютно пуста. Начинаем глобальный парсинг всех сайтов... <<<");

        // 1. Запускаем стандартные парсеры (HabrService и др.), которые реализуют ParsingSites
        for (ParsingSites scraper : scrapers) {
            System.out.println("Запуск парсера: " + scraper.getClass().getSimpleName());

            var data = scraper.scrape();

            questionService.saveQuestions(data);
        }

        // 2. Запускаем наш автономный комбайн для ITVDN
        System.out.println("Запуск парсера: ItvdnService (автономный)");
        var itvdnData = itvdnService.scrape();

        questionService.saveQuestions(itvdnData);

        System.out.println("Запуск парсера: PythonItvdnService");
        var pythonData = pythonItvdnService.scrape();
        questionService.saveQuestions(pythonData);

        System.out.println("Запуск парсера: MachineLearningLabexService");
        var mlData = machineLearningLabexService.scrape();
        questionService.saveQuestions(mlData);

        System.out.println("Запуск парсера: NoSqlHabrService");
        var nosqlData = noSqlHabrService.scrape();
        questionService.saveQuestions(nosqlData);

        System.out.println("Запуск парсера: NoSqlHabrService");
        var iosData = iosHabrService.scrape();
        questionService.saveQuestions(iosData);

        System.out.println(">>> Глобальный парсинг успешно завершен! База заполнена. <<<");
    }
}
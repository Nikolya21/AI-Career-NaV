package org.example.aicareernav1.dto.questionDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.repository.ParsingSites;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.service.scraper.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
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
    private final KataGoService kataGoService;
    // Внедряем ВСЕ зависимости через один конструктор (лучшая практика Spring)

    @Override
    public void run(String... args) throws Exception {

        // ПРОВЕРКА: Если в базе УЖЕ есть хоть один вопрос — выходим.
        if (questionRepository.count() > 0) {
            log.info(">>> База данных уже заполнена! Парсинг больше не требуется. <<<");
            return;
        }

        log.info(">>> База абсолютно пуста. Начинаем глобальный парсинг всех сайтов... <<<");

        // 1. Запускаем стандартные парсеры (HabrService и др.), которые реализуют ParsingSites
        for (ParsingSites scraper : scrapers) {
            log.info("Запуск парсера: " + scraper.getClass().getSimpleName());

            var data = scraper.scrape();

            questionService.saveQuestions(data);
        }

        // 2. Запускаем наш автономный комбайн для ITVDN
        log.info("Запуск парсера: ItvdnService (автономный)");
        var itvdnData = itvdnService.scrape();

        questionService.saveQuestions(itvdnData);

        log.info("Запуск парсера: PythonItvdnService");
        var pythonData = pythonItvdnService.scrape();
        questionService.saveQuestions(pythonData);

        log.info("Запуск парсера: MachineLearningLabexService");
        var mlData = machineLearningLabexService.scrape();
        questionService.saveQuestions(mlData);

        log.info("Запуск парсера: NoSqlHabrService");
        var nosqlData = noSqlHabrService.scrape();
        questionService.saveQuestions(nosqlData);

        log.info("Запуск парсера: Ios/Swift");
        var iosData = iosHabrService.scrape();
        questionService.saveQuestions(iosData);

        log.info("Запуск парсера: GoService");
        var goDevelop = kataGoService.scrape();
        questionService.saveQuestions(goDevelop);

        log.info(">>> Глобальный парсинг успешно завершен! База заполнена. <<<");
    }
}
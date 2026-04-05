package org.example.aicareernav1.dto.questionDto;

import org.example.aicareernav1.repository.ParsingSites;
import org.example.aicareernav1.repository.QuestionRepository;
import org.example.aicareernav1.service.Scraper.ItvdnService;
import org.example.aicareernav1.service.Scraper.QuestionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private final QuestionRepository questionRepository;
    private final List<ParsingSites> scrapers;
    private final ItvdnService itvdnService;
    private final QuestionService questionService;

    // Внедряем ВСЕ зависимости через один конструктор (лучшая практика Spring)
    public DatabaseInitializer(QuestionRepository questionRepository,
                               List<ParsingSites> scrapers,
                               ItvdnService itvdnService,
                               QuestionService questionService) {
        this.questionRepository = questionRepository;
        this.scrapers = scrapers;
        this.itvdnService = itvdnService;
        this.questionService = questionService;
    }

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
        var itvdnData = itvdnService.scrape(); // Выгребает Java и C++ по очереди

        questionService.saveQuestions(itvdnData); // Сохраняет всю эту пачку в БД

        System.out.println(">>> Глобальный парсинг успешно завершен! База заполнена. <<<");
    }
}
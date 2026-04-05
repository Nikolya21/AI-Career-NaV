package org.example.aicareernav1.service.Scraper;

import org.example.aicareernav1.dto.questionDto.ParsedDataDto;
import org.example.aicareernav1.repository.ParsingSites;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HabrService implements ParsingSites {

    // Тестовая мапа URL -> Язык. Ты можешь добавить сюда другие статьи с такой же версткой.
    private static final Map<String, String> URL_TO_TECH_MAP = new HashMap<>();

    static {
        URL_TO_TECH_MAP.put("https://habr.com/ru/sandbox/151396/", "PHP");
    }

    @Override
    public List<ParsedDataDto> scrape() {
        List<ParsedDataDto> allResults = new ArrayList<>();

        for (Map.Entry<String, String> entry : URL_TO_TECH_MAP.entrySet()) {
            String url = entry.getKey();
            String targetLanguage = entry.getValue();

            System.out.println("Парсим Хабр напрямую через Jsoup для " + targetLanguage + "...");

            try {
                // Jsoup САМ делает запрос на сервер без всякого Selenium!
                Document doc = Jsoup.connect(url)
                  .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                  .timeout(5000)
                  .get();

                // Вызываем твой готовый метод парсинга
                List<ParsedDataDto> parsedQuestions = parseDynamicDifficultyHtml(doc, targetLanguage);

                System.out.println("Найдено вопросов на странице: " + parsedQuestions.size());
                allResults.addAll(parsedQuestions);

            } catch (Exception e) {
                System.err.println("Ошибка при загрузке страницы: " + url);
                e.printStackTrace();
            }
        }
        return allResults;
    }

    /**
     * НОВАЯ ЛОГИКА: Сканируем HTML линейно и определяем сложность на ходу.
     */
    private List<ParsedDataDto> parseDynamicDifficultyHtml(Document doc, String language) {
        List<ParsedDataDto> pageResults = new ArrayList<>();

        // Ищем главный контейнер статьи
        Element articleBody = doc.selectFirst(".post-content-body");
        if (articleBody == null) {
            articleBody = doc.selectFirst(".article-formatted-body");
        }

        if (articleBody == null) {
            System.out.println("❌ Не удалось найти тело статьи на Хабре!");
            return pageResults;
        }

        // По умолчанию ставим Junior, так как статья начинается с него
        String currentDifficulty = "Junior";

        // Вытаскиваем ВСЕ элементы h2, h3 и ol, которые есть внутри статьи,
        // плевать на то, насколько глубоко они зарыты!
        Elements elements = articleBody.select("h2, h3, ol");
        System.out.println("Найдено заголовков и списков: " + elements.size());

        for (Element el : elements) {

            // Шаг 1: Если это заголовок — проверяем и меняем уровень сложности
            if (el.tagName().equals("h2") || el.tagName().equals("h3")) {
                String headerText = el.text().trim();

                if (headerText.equalsIgnoreCase("Junior") ||
                  headerText.equalsIgnoreCase("Middle") ||
                  headerText.equalsIgnoreCase("Senior")) {

                    currentDifficulty = headerText;
                    System.out.println("--- Переключили сложность на: " + currentDifficulty + " ---");
                }
            }

            // Шаг 2: Если это список — забираем из него вопросы
            else if (el.tagName().equals("ol")) {
                Elements listItems = el.select("li");

                for (Element li : listItems) {
                    String questionText = li.text().trim();

                    // Убираем нумерацию (например, "1. ")
                    questionText = questionText.replaceFirst("^\\d+\\.\\s*", "");

                    if (questionText.isEmpty() || questionText.length() < 10) {
                        continue;
                    }

                    Set<String> tags = new HashSet<>();
                    tags.add(language); // Наш язык (PHP)

                    // Создаем DTO и отправляем в список
                    pageResults.add(new ParsedDataDto(questionText, currentDifficulty, tags));
                }
            }
        }

        System.out.println("Успешно спарсили вопросов: " + pageResults.size());
        return pageResults;
    }

    @Override
    public boolean supports(String siteName) {
        return "habr".equalsIgnoreCase(siteName);
    }
}
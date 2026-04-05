package org.example.aicareernav1.service.Scraper;

import org.example.aicareernav1.dto.questionDto.ParsedDataDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PythonItvdnService {

    private static final String URL = "https://itvdn.com/ru/blog/article/interview-questions-python-developer";

    public List<ParsedDataDto> scrape() {
        List<ParsedDataDto> results = new ArrayList<>();
        System.out.println("🚀 Начинаем парсинг ITVDN для Python...");

        try {
            Document doc = Jsoup.connect(URL)
              .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
              .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
              .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
              .header("Cache-Control", "no-cache")
              .header("Connection", "keep-alive")
              .timeout(10000)
              .get();

            Element articleBody = doc.selectFirst(".article-text article");
            if (articleBody == null) {
                articleBody = doc.selectFirst(".article-text");
            }

            if (articleBody == null) {
                System.out.println("❌ Не удалось найти тело статьи на ITVDN для Python!");
                return results;
            }

            // ПОПРАВКА ТУТ: Изначально уровень неизвестен
            String currentDifficulty = "Unknown";

            Elements elements = articleBody.select("h2, p");
            System.out.println("Найдено элементов (h2 и p) для анализа: " + elements.size());

            for (Element el : elements) {
                String text = el.text().trim();
                if (text.isEmpty()) continue;

                // 1. Если элемент — это h2, мы ТОЧНО знаем, что уровень сменился
                if (el.tagName().equalsIgnoreCase("h2")) {
                    String lowerText = text.toLowerCase();

                    if (lowerText.contains("junior")) {
                        currentDifficulty = "Junior";
                        System.out.println("--- Уровень установлен: " + currentDifficulty + " ---");
                    } else if (lowerText.contains("middle")) {
                        currentDifficulty = "Middle";
                        System.out.println("--- Уровень установлен: " + currentDifficulty + " ---");
                    } else if (lowerText.contains("senior")) {
                        currentDifficulty = "Senior";
                        System.out.println("--- Уровень установлен: " + currentDifficulty + " ---");
                    }
                    continue;
                }

                // 2. Если это вопрос (p, начинающийся с цифры)
                if (el.tagName().equalsIgnoreCase("p") && text.matches("^\\d+\\.\\s*.*")) {
                    String questionText = text.replaceFirst("^\\d+\\.\\s*", "");

                    if (questionText.length() < 10) continue;

                    Set<String> tags = new HashSet<>();
                    tags.add("Python");

                    results.add(new ParsedDataDto(questionText, currentDifficulty, tags));
                }
            }

            System.out.println("✅ Успешно спарсили вопросов по Python: " + results.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка при парсинге Python: " + e.getMessage());
        }

        return results;
    }
}
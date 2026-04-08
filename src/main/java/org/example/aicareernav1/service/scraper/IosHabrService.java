package org.example.aicareernav1.service.scraper;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IosHabrService {

    private static final String URL = "https://habr.com/ru/articles/726388/";

    public List<ParsedDataDto> scrape() {
        List<ParsedDataDto> results = new ArrayList<>();
        System.out.println("🚀 Начинаем парсинг Хабра для iOS...");

        try {
            Document doc = Jsoup.connect(URL)
              .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
              .timeout(10000)
              .get();

            Element articleBody = doc.selectFirst(".article-formatted-body");
            if (articleBody == null) {
                System.out.println("❌ Не удалось найти тело статьи на Хабре!");
                return results;
            }

            // Находим все теги h3, так как именно в них лежат вопросы
            Elements questions = articleBody.select("h3");
            System.out.println("Найдено потенциальных вопросов (h3): " + questions.size());

            for (Element q : questions) {
                String text = q.text().trim();

                int questionNumber = 0;
                Matcher matcher = Pattern.compile("^(\\d+)[\\).\\s]").matcher(text);
                if (matcher.find()) {
                    questionNumber = Integer.parseInt(matcher.group(1));
                }

                // Если номер вытащить не удалось (вдруг попался h4 без номера), пропускаем
                if (questionNumber == 0) continue;
                // Убираем нумерацию в начале (например, "1. What frameworks...")
                String questionText = text.replaceFirst("^\\d+\\.\\s*", "");

                if (questionText.isEmpty() || questionText.length() < 10) {
                    continue;
                }

                Set<String> tags = new HashSet<>();
                tags.add("iOS");
                tags.add("Swift");

                // Так как в статье нет деления по уровням, ставим Unknown
                String difficulty;
                if (questionNumber <= 13) {
                    difficulty = "Junior";
                } else if (questionNumber <= 28) {
                    difficulty = "Middle";
                } else if (questionNumber <= 40) {
                    difficulty = "Senior";
                } else {
                    difficulty = "Unknown";
                }

                results.add(new ParsedDataDto(questionText, difficulty, tags));
            }

            System.out.println("✅ Успешно спарсили вопросов по iOS: " + results.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка при парсинге iOS: " + e.getMessage());
        }

        return results;
    }
}
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NoSqlHabrService {

    private static final String URL = "https://habr.com/ru/companies/otus/articles/768342/";

    public List<ParsedDataDto> scrape() {
        List<ParsedDataDto> results = new ArrayList<>();
        System.out.println("🚀 Начинаем парсинг Хабра для NoSQL...");

        try {
            Document doc = Jsoup.connect(URL)
              .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
              .timeout(10000)
              .get();

            // Ищем контейнер с текстом статьи на Хабре
            Element articleBody = doc.selectFirst(".article-formatted-body");
            if (articleBody == null) {
                System.out.println("❌ Не удалось найти тело статьи на Хабре!");
                return results;
            }

            // Находим все теги h4, в которых лежат вопросы
            Elements questionElements = articleBody.select("h4");
            System.out.println("Найдено потенциальных вопросов (h4): " + questionElements.size());

            for (Element q : questionElements) {
                String text = q.text().trim();
                if (text.isEmpty()) continue;

                // Извлекаем цифру из начала вопроса (например, из "1) Что такое..." или "1. Что такое...")
                int questionNumber = 0;
                Matcher matcher = Pattern.compile("^(\\d+)[\\).\\s]").matcher(text);
                if (matcher.find()) {
                    questionNumber = Integer.parseInt(matcher.group(1));
                }

                // Если номер вытащить не удалось (вдруг попался h4 без номера), пропускаем
                if (questionNumber == 0) continue;

                // Убираем нумерацию из самого вопроса для сохранения в базу
                String questionText = text.replaceFirst("^\\d+[\\).\\s]+", "");
                if (questionText.length() < 5) continue;

                // Твоя логика распределения по уровням
                String difficulty;
                if (questionNumber <= 15) {
                    difficulty = "Junior";
                } else if (questionNumber <= 40) {
                    difficulty = "Middle";
                } else if (questionNumber <= 82) {
                    difficulty = "Senior";
                } else {
                    difficulty = "Unknown"; // На случай, если вопросов окажется больше 82
                }

                Set<String> tags = new HashSet<>();
                tags.add("NoSQL");
                tags.add("MongoDB");

                results.add(new ParsedDataDto(questionText, difficulty, tags));
            }

            System.out.println("✅ Успешно спарсили вопросов по NoSQL: " + results.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка при парсинге NoSQL: " + e.getMessage());
        }

        return results;
    }
}
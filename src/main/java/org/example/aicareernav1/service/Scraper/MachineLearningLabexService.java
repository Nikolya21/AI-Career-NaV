package org.example.aicareernav1.service.Scraper;

import org.example.aicareernav1.dto.questionDto.ParsedDataDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MachineLearningLabexService {

    private static final String URL = "https://labex.io/ru/tutorials/ml-machine-learning-interview-questions-and-answers-593691";

    public List<ParsedDataDto> scrape() {
        List<ParsedDataDto> results = new ArrayList<>();
        System.out.println("🚀 Начинаем парсинг LabEx для Machine Learning...");

        try {
            File input = new File("ml.html");
            Document doc = Jsoup.parse(input, "UTF-8", "https://labex.io/");

            // Ищем контейнер с текстом статьи
            Element articleBody = doc.selectFirst("article#md-position-1-preview");
            if (articleBody == null) {
                articleBody = doc.selectFirst(".md-editor-preview-wrapper");
            }

            if (articleBody == null) {
                System.out.println("❌ Не удалось найти тело статьи на LabEx!");
                return results;
            }

            // Находим все теги h3, так как именно в них лежат вопросы
            Elements questions = articleBody.select("h3");
            System.out.println("Найдено потенциальных вопросов (h3): " + questions.size());

            for (Element q : questions) {
                String questionText = q.text().trim();

                // Убираем нумерацию в начале, если она случайно попадется (например, "1. Что такое...")
                questionText = questionText.replaceFirst("^\\d+\\.\\s*", "");

                if (questionText.isEmpty() || questionText.length() < 10) {
                    continue;
                }

                Set<String> tags = new HashSet<>();
                tags.add("Machine Learning");

                // Уровень не подтвержден (ставим Unknown, как ты и хотел)
                String difficulty = "Unknown";

                results.add(new ParsedDataDto(questionText, difficulty, tags));
            }

            System.out.println("✅ Успешно спарсили вопросов по ML: " + results.size());

        } catch (Exception e) {
            System.err.println("❌ Ошибка при парсинге LabEx (ML): " + e.getMessage());
        }

        return results;
    }
}
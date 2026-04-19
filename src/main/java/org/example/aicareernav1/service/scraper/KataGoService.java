package org.example.aicareernav1.service.scraper;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class KataGoService {

    private static final String URL = "https://kata.academy/blog/hrhunting/voprosy-po-go-na-sobesedovanii";

    public List<ParsedDataDto> scrape() {
        List<ParsedDataDto> results = new ArrayList<>();
        log.info("🚀 Начинаем парсинг Kata Academy для Go...");

        try {
            Document doc = Jsoup.connect(URL)
              .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
              .timeout(10000)
              .get();

            Elements sections = doc.select(".r");
            String currentDifficulty = "Junior";

            // Флаг, чтобы не парсить "преимущества" в начале страницы
            boolean isQuestionSectionStarted = false;

            for (Element section : sections) {
                // 1. Логика определения раздела и уровня
                Element titleElement = section.selectFirst("h2, h3, .t-title, .t-name");
                if (titleElement != null) {
                    String titleText = titleElement.text().toLowerCase();

                    // Активируем парсинг только когда дошли до блока с вопросами
                    if (titleText.contains("вопросы")) {
                        isQuestionSectionStarted = true;
                    }

                    if (titleText.contains("middle")) {
                        currentDifficulty = "Middle";
                        log.info("--- Уровень установлен: {} ---", currentDifficulty);
                    } else if (titleText.contains("senior")) {
                        currentDifficulty = "Senior";
                        log.info("--- Уровень установлен: {} ---", currentDifficulty);
                    }
                }

                // Если мы еще не дошли до раздела вопросов — пропускаем блок
                if (!isQuestionSectionStarted) continue;

                // 2. Парсинг контента
                Elements textBlocks = section.select("div[field='text'], div.t-text");

                for (Element block : textBlocks) {
                    block.select("br").append("\\n");
                    block.select("p").prepend("\\n");

                    String textContent = block.text().replace("\\n", "\n");
                    String[] lines = textContent.split("\n");

                    for (String line : lines) {
                        String cleanLine = line.trim();

                        // Проверка на тире в начале
                        if (cleanLine.matches("^[—\\-\\u2013\\u2014].*")) {
                            String questionText = cleanLine.replaceFirst("^[—\\-\\u2013\\u2014]", "").trim();

                            // ГЛАВНЫЙ ФИЛЬТР:
                            // 1. Длина > 10
                            // 2. Наличие знака вопроса (отсекаем повествовательные предложения)
                            if (questionText.length() > 10 && questionText.contains("?")) {
                                Set<String> tags = new HashSet<>();
                                tags.add("Go");

                                results.add(new ParsedDataDto(questionText, currentDifficulty, tags));
                            }
                        }
                    }
                }
            }

            log.info("✅ Успешно спарсили вопросов по Go: {}", results.size());

        } catch (Exception e) {
            log.error("❌ Ошибка при парсинге Go (Kata): {}", e.getMessage());
        }

        return results;
    }
}
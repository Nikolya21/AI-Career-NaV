package org.example.aicareernav1.service.scraper;

import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.questionDto.ParsedDataDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ItvdnService {

    private static final Map<String, String> URLS_TO_PARSE = Map.of(
      "https://itvdn.com/ru/blog/article/250-questions-java", "Java",
      "https://itvdn.com/ru/blog/article/400-about-cplspls", "C++",
      "https://itvdn.com/ru/blog/article/250-questions-qa", "QA",
      "https://itvdn.com/ru/blog/article/250-about-android", "Android-разработчик",
      "https://itvdn.com/ru/blog/article/300-js", "JavaScript",
      "https://itvdn.com/ru/blog/article/ruby-500-questions", "Ruby",
      "https://itvdn.com/ru/blog/article/300-devops", "Devops",
      "https://itvdn.com/ru/blog/article/150-questions-net-developer", ".NET"
    );

    public List<ParsedDataDto> scrape() {
        List<ParsedDataDto> allResults = new ArrayList<>();

        // Цикл идет по всем ссылкам из нашей карты
        for (Map.Entry<String, String> entry : URLS_TO_PARSE.entrySet()) {
            String url = entry.getKey();
            String languageTag = entry.getValue();

            log.info("🚀 Начинаем парсинг ITVDN для стека [" + languageTag + "]...");

            try {
                Document doc = Jsoup.connect(url)
                  .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                  .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                  .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                  .header("Cache-Control", "no-cache")
                  .header("Connection", "keep-alive")
                  .timeout(10000)
                  .get();

                Element articleBody = doc.selectFirst(".article-text");
                if (articleBody == null) {
                    log.info("❌ Не нашли тело статьи на: " + url);
                    continue; // Пропускаем эту ссылку и идем к следующей
                }

                String currentDifficulty = "Junior";
                Elements paragraphs = articleBody.select("p");
                int countForThisUrl = 0;

                for (Element p : paragraphs) {
                    String text = p.text().trim();
                    if (text.isEmpty()) continue;

                    // Проверяем заголовки сложности
                    Element strongElement = p.selectFirst("strong");
                    if (strongElement != null && strongElement.text().trim().equals(text)) {
                        if (text.equalsIgnoreCase("Junior") ||
                          text.equalsIgnoreCase("Middle") ||
                          text.equalsIgnoreCase("Senior")) {

                            currentDifficulty = text;
                            continue;
                        }
                    }

                    // Ищем вопросы (начинаются с цифры и точки)
                    if (text.matches("^\\d+\\.\\s*.*")) {
                        String questionText = text.replaceFirst("^\\d+\\.\\s*", "");

                        if (questionText.length() < 10) continue;

                        Set<String> tags = new HashSet<>();
                        tags.add(languageTag); // Присваиваем тег текущего языка

                        allResults.add(new ParsedDataDto(questionText, currentDifficulty, tags));
                        countForThisUrl++;
                    }
                }

                log.info("✅ Успешно спарсили " + countForThisUrl + " вопросов по тегу [" + languageTag + "]");

            } catch (Exception e) {
                System.err.println("❌ Ошибка при парсинге URL [" + url + "]: " + e.getMessage());
            }
        }

        log.info("🏁 Глобальный парсинг ITVDN завершен! Всего собрано вопросов: " + allResults.size());
        return allResults;
    }
}
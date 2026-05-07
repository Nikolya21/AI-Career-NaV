package org.example.aicareernav1.service.parser;

import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.hhDto.HhKeySkill;
import org.example.aicareernav1.dto.hhDto.HhSalary;
import org.example.aicareernav1.dto.hhDto.HhVacanciesResponse;
import org.example.aicareernav1.dto.hhDto.HhVacancyItem;
import org.example.aicareernav1.model.vacancy.RealVacancy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParserService {

  private final WebClient hhWebClient;
  private final WebClient habrWebClient = WebClient.builder().baseUrl("https://career.habr.com").build();

  private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 AICareerNav/1.0 (qolchenkolaex@gmail.com)";

  public ParserService(WebClient hhWebClient) {
    this.hhWebClient = hhWebClient;
  }

  public List<RealVacancy> getVacancies(String searchText, String area, int perPage) {
    List<RealVacancy> allVacancies = new ArrayList<>();

    // 1. Пытаемся получить данные с HeadHunter
    try {
      log.info("Запрос к HH.ru для: {}", searchText);
      List<HhVacancyItem> hhItems = fetchHhVacancies(searchText, area, perPage);
      if (hhItems != null && !hhItems.isEmpty()) {
        int limit = Math.min(hhItems.size(), 3);
        for (int i = 0; i < limit; i++) {
          HhVacancyItem detailed = fetchHhVacancyDetails(hhItems.get(i).id());
          if (detailed != null) allVacancies.add(mapToRealVacancy(detailed, "HH.ru"));
          Thread.sleep(1000); // Пауза во избежание блокировок
        }
      }
    } catch (Exception e) {
      log.error("HH.ru недоступен или заблокирован: {}", e.getMessage());
    }

    // 2. Реальный парсинг Хабр Карьеры (через RSS)
    try {
      log.info("Запрос к Хабр Карьере для: {}", searchText);
      String xmlData = habrWebClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/vacancies/rss")
              .queryParam("q", searchText)
              .queryParam("type", "all")
              .build())
          .header(HttpHeaders.USER_AGENT, USER_AGENT)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      if (xmlData != null) {
        allVacancies.addAll(parseHabrXml(xmlData));
      }
    } catch (Exception e) {
      log.error("Ошибка при парсинге Хабра: {}", e.getMessage());
    }

    // 3. Если всё пусто — выдаем заглушку
    if (allVacancies.isEmpty()) {
      allVacancies.add(getMockVacancy(searchText));
    }

    return allVacancies;
  }

  private List<RealVacancy> parseHabrXml(String xmlData) throws Exception {
    List<RealVacancy> vacancies = new ArrayList<>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
    NodeList nodes = doc.getElementsByTagName("item");

    for (int i = 0; i < Math.min(nodes.getLength(), 5); i++) {
      Element element = (Element) nodes.item(i);
      String title = element.getElementsByTagName("title").item(0).getTextContent();
      String description = element.getElementsByTagName("description").item(0).getTextContent();

      vacancies.add(new RealVacancy(
          title + " [Habr]",
          List.of("Habr", "IT"),
          "См. на сайте",
          "Не указано",
          "18+",
          "Хабр Карьера",
          description.replaceAll("<[^>]*>", "").substring(0, Math.min(description.length(), 300)) + "..."
      ));
    }
    return vacancies;
  }

  private List<HhVacancyItem> fetchHhVacancies(String searchText, String area, int perPage) {
    return hhWebClient.get()
        .uri(uriBuilder -> uriBuilder.path("/vacancies")
            .queryParam("text", searchText)
            .queryParam("area", area)
            .queryParam("per_page", perPage).build())
        .header(HttpHeaders.USER_AGENT, USER_AGENT)
        .retrieve()
        .onStatus(HttpStatusCode::isError, resp -> Mono.empty())
        .bodyToMono(HhVacanciesResponse.class)
        .map(HhVacanciesResponse::items)
        .onErrorReturn(Collections.emptyList())
        .block();
  }

  private HhVacancyItem fetchHhVacancyDetails(String id) {
    return hhWebClient.get().uri("/vacancies/{id}", id)
        .header(HttpHeaders.USER_AGENT, USER_AGENT)
        .retrieve()
        .bodyToMono(HhVacancyItem.class)
        .onErrorResume(e -> Mono.empty())
        .block();
  }

  private RealVacancy mapToRealVacancy(HhVacancyItem item, String source) {
    String salary = formatSalary(item.salary());
    List<String> skills = item.keySkills() != null
        ? item.keySkills().stream().map(HhKeySkill::name).collect(Collectors.toList())
        : Collections.emptyList();

    return new RealVacancy(
        item.name() + " [" + source + "]",
        skills,
        salary,
        item.experience() != null ? item.experience().name() : "Не указано",
        "18+",
        item.employer() != null ? item.employer().name() : "Неизвестно",
        item.description() != null ? item.description().toString() : ""
    );
  }

  private String formatSalary(HhSalary salary) {
    if (salary == null) return "Зарплата не указана";
    if (salary.from() != null && salary.to() != null) return salary.from() + " - " + salary.to() + " " + salary.currency();
    if (salary.from() != null) return "от " + salary.from() + " " + salary.currency();
    return "до " + salary.to() + " " + salary.currency();
  }

  private RealVacancy getMockVacancy(String searchText) {
    return new RealVacancy(
        searchText + " (Резервный канал)",
        List.of("Java", "SQL", "Git"),
        "от 120 000 руб.",
        "Без опыта",
        "18+",
        "Career Nav AI",
        "Временная техническая заглушка. Проверьте подключение к сети."
    );
  }
}
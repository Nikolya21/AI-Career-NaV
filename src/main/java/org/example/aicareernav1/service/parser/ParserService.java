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
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParserService {

  private final WebClient hhWebClient;

  public ParserService(WebClient hhWebClient) {
    this.hhWebClient = hhWebClient;
  }

  public List<RealVacancy> getVacancies(String searchText, String area, int perPage) {
    log.info("Запрос вакансий: searchText={}, area={}, perPage={}", searchText, area, perPage);
    List<HhVacancyItem> items = fetchVacancies(searchText, area, perPage);
    return items.stream()
        .map(this::mapToRealVacancy)
        .collect(Collectors.toList());
  }

  private List<HhVacancyItem> fetchVacancies(String searchText, String area, int perPage) {
    try {
      URI uri = UriComponentsBuilder.fromUriString("https://api.hh.ru")
          .path("/vacancies")
          .queryParam("text", searchText)
          .queryParam("area", area)
          .queryParam("per_page", perPage)
          .build()
          .encode()
          .toUri();

      log.info("🔗 Полный URL запроса: {}", uri);

      HhVacanciesResponse response = hhWebClient.get()
          .uri(uri)
          .header(HttpHeaders.USER_AGENT, "HH-Parser/1.0")
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
            return clientResponse.bodyToMono(String.class)
                .<Throwable>flatMap(error -> Mono.error(
                    new RuntimeException("Client error: " + error)));
          })
          .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
              Mono.error(new RuntimeException("Server error from hh.ru")))
          .bodyToMono(HhVacanciesResponse.class)
          .block();

      return response != null ? response.items() : Collections.emptyList();
    } catch (Exception e) {
      log.error("Ошибка при запросе к hh.ru: {}", e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private RealVacancy mapToRealVacancy(HhVacancyItem item) {
    String salary = formatSalary(item.salary());
    String experience = item.experience() != null ? item.experience().name() : "Не указано";
    String age = item.ageRestriction() != null ? item.ageRestriction().name() : "Не указано";
    String employer = item.employer() != null ? item.employer().name() : "Не указано";
    List<String> skills = item.keySkills() != null
        ? item.keySkills().stream().map(HhKeySkill::name).collect(Collectors.toList())
        : Collections.emptyList();

    return new RealVacancy(
        item.name(),
        skills,
        salary,
        experience,
        age,
        employer
    );
  }

  private String formatSalary(HhSalary salary) {
    if (salary == null) return null;
    if (salary.from() != null && salary.to() != null) {
      return salary.from() + " - " + salary.to() + " " + salary.currency();
    } else if (salary.from() != null) {
      return "от " + salary.from() + " " + salary.currency();
    } else if (salary.to() != null) {
      return "до " + salary.to() + " " + salary.currency();
    }
    return null;
  }
}
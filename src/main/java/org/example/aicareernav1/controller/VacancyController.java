package org.example.aicareernav1.controller;

import org.example.aicareernav1.dto.response.VacancyResponse;
import org.example.aicareernav1.model.vacancy.RealVacancy;
import org.example.aicareernav1.service.parser.ParserService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/vacancies")
@RequiredArgsConstructor
public class VacancyController {

  private final ParserService parserService;

  @GetMapping
  public ResponseEntity<List<VacancyResponse>> getVacancies(@RequestParam String searchText,
      @RequestParam(defaultValue = "1") String area,
      @RequestParam(defaultValue = "10") int perPage) {
    log.info("GET /vacancies - searchText={}, area={}, perPage={}", searchText, area, perPage);
    List<RealVacancy> vacancies = parserService.getVacancies(searchText, area, perPage);
    List<VacancyResponse> response = vacancies.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    log.info("Успешно получено {} вакансий", response.size());
    return ResponseEntity.ok(response);

  }

  private VacancyResponse mapToResponse(RealVacancy vacancy) {
    return VacancyResponse.builder()
        .nameOfVacancy(vacancy.getNameOfVacancy())
        .vacancyRequirements(vacancy.getVacancyRequirements())
        .salary(vacancy.getSalary())
        .experience(vacancy.getExperience())
        .age(vacancy.getAge())
        .employer(vacancy.getEmployer())
        .description(vacancy.getDescription())
        .build();
  }
}

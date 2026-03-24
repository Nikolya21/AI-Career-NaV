package org.example.aicareernav1.dto.hhDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties
public record HhVacanciesResponse(List<HhVacancyItem> items) {}


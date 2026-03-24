package com.aicareer.core.dto.hhDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties
public record HhVacanciesResponse(List<HhVacancyItem> items) {}


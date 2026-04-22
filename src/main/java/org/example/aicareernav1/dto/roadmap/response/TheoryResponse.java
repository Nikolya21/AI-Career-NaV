package org.example.aicareernav1.dto.roadmap.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TheoryResponse {
  private String text;
  private List<String> tags;
  private List<ResourceResponse> resources;
}

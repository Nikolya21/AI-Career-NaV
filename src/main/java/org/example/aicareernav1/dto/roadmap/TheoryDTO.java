package org.example.aicareernav1.dto.roadmap;

import lombok.Data;

import java.util.List;

@Data
public class TheoryDTO {
  private String text;
  private List<String> tags;
  private List<ResourceDTO> resources;
}

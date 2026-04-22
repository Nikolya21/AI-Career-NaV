package org.example.aicareernav1.dto.roadmap;

import lombok.Data;
import org.example.aicareernav1.enums.ResourceType;

@Data
public class ResourceDTO {
  private String title;
  private String url;
  private ResourceType type;
}
package org.example.aicareernav1.dto.roadmap.response;

import lombok.Data;
import org.example.aicareernav1.dto.roadmap.TopicDTO;

import java.util.List;

@Data
public class SkeletonResponse {
  private List<TopicDTO> topics;
}
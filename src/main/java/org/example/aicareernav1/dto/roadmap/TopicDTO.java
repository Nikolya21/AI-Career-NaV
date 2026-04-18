package org.example.aicareernav1.dto.roadmap;

import lombok.Data;
import org.example.aicareernav1.dto.roadmap.checkpoint.CheckpointDTO;

import java.util.List;

@Data
public class TopicDTO {
  private String topicTitle;
  private List<CheckpointDTO> checkpoints;
}
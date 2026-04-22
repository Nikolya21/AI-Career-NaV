package org.example.aicareernav1.dto.roadmap.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponse {
    private Long id;
    private String topicTitle;
    private Integer orderIndex;
    private List<CheckpointResponse> checkpoints; // Все этапы внутри этой темы
}
package org.example.aicareernav1.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoadmapSkeletonDTO {
    // Список топиков (крупных разделов)
    private List<TopicSkeletonDTO> topics;

    @Data
    public static class TopicSkeletonDTO {
        private String topicTitle;
        // Список конкретных шагов внутри раздела
        private List<CheckpointSkeletonDTO> checkpoints;
    }

    @Data
    public static class CheckpointSkeletonDTO {
        private String title;
        private String description;
    }
}

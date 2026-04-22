package org.example.aicareernav1.dto.roadmap.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aicareernav1.dto.roadmap.config.RoadmapConfigDTO; // Предположим, есть DTO для конфига
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapResponse {
    private Long id;
    private String targetJobTitle;
    private Double progress;
    private RoadmapConfigDTO config;
    private List<TopicResponse> topics; // Список топиков с вложенными чекпоинтами (точки входа в граф)
    private Integer totalCheckpoints; // Общее количество узлов (этапов) в графе
}
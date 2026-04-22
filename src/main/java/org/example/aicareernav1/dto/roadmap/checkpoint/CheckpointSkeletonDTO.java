package org.example.aicareernav1.dto.roadmap.checkpoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointSkeletonDTO {
    private String title;
    private String description;
    private List<String> lessonTitles; // Список названий будущих уроков
}

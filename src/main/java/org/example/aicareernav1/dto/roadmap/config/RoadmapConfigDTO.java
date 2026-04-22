package org.example.aicareernav1.dto.roadmap.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapConfigDTO {
    private Long id;
    private String mainDomain;    // Например: Java Development
    private String targetLevel;  // Например: Junior
    private String learningStyle; // Например: Code-heavy
    private String toneOfVoice;  // Например: Friendly mentor
}

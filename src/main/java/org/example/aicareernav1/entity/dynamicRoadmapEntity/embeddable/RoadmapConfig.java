package org.example.aicareernav1.entity.dynamicRoadmapEntity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapConfig {

    @Column(name = "main_domain", nullable = false)
    private String mainDomain; // Java, Python, UI/UX

    @Column(name = "target_level", nullable = false)
    private String targetLevel; // Beginner, Junior, Middle, Senior

    @Column(name = "learning_style", columnDefinition = "TEXT")
    private String learningStyle; // "Loves Analogies, Needs Code Samples"

    @Column(name = "max_tags")
    @Builder.Default
    private Integer maxTags = 5;

    public String asPromptTags() {
        // Добавим проверку на null, чтобы промпт не содержал слова "null"
        return String.format("%s, %s, %s",
                mainDomain != null ? mainDomain : "General IT",
                targetLevel != null ? targetLevel : "Beginner",
                learningStyle != null ? learningStyle : "Standard style");
    }
}

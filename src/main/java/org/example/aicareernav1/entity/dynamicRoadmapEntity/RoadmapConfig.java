package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "roadmap_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadmapConfig {

    @Column(name = "main_domain", nullable = false)
    private String mainDomain; // Java, Python, UI/UX

    @Column(name = "target_level", nullable = false)
    private String targetLevel; // Beginner, Junior, Middle, Senior

    @Column(name = "learning_style", columnDefinition = "TEXT")
    private String learningStyle; // "Loves Analogies, Needs Code Samples"

    @Column(name = "tone_of_voice")
    private String toneOfVoice; // характер общения: дружилюбные наставний, строгий учитель и тп

    @Column(name = "max_tags")
    @Builder.Default
    private Integer maxTags = 5;

    // Связь обратно к Roadmap, если нужно (optional)
    @OneToOne(mappedBy = "learningProfile")
    private Roadmap roadmap;
}

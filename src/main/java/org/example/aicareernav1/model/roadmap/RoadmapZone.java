package org.example.aicareernav1.model.roadmap;

import org.example.aicareernav1.model.courseModel.Week;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapZone {
    private Long id;
    private Long roadmapId;
    private String name;
    private List<Week> weeks = new ArrayList<>();
    private String learningGoal;
    private String complexityLevel;
    private Integer zoneOrder;
    private Instant createdAt; // ← Изменил на Instant

    public void addWeek(Week week) {
        if (this.weeks == null) {
            this.weeks = new ArrayList<>();
        }
        this.weeks.add(week);
    }
    public void updateTimestamps() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}

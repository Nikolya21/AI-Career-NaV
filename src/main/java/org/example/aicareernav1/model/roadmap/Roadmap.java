package org.example.aicareernav1.model.roadmap;


import org.example.aicareernav1.model.courseModel.Week;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Roadmap {
    private Long id;
    private Long userId;
    private List<RoadmapZone> roadmapZones = new ArrayList<>();
    private Instant createdAt;  // ← Изменил на Instant для consistency
    private Instant updatedAt;  // ← Изменил на Instant

    public void addRoadmapZone(RoadmapZone zone) {
        if (this.roadmapZones == null) {
            this.roadmapZones = new ArrayList<>();
        }
        this.roadmapZones.add(zone);
    }

    public void updateTimestamps() {
        this.updatedAt = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
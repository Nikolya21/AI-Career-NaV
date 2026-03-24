package com.aicareer.core.model.roadmap;


import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
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
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Roadmap #").append(" (User: ").append(userId).append(")\n");

        if (roadmapZones != null) {
            for (int i = 0; i < roadmapZones.size(); i++) {
                RoadmapZone zone = roadmapZones.get(i);
                boolean isLastZone = i == roadmapZones.size() - 1;

                sb.append(isLastZone ? "└── " : "├── ")
                        .append("Zone ").append(i + 1).append(": ").append(zone.getName()).append("\n");
                sb.append(isLastZone ? "    " : "│   ").append("├── Weeks: ").append(zone.getWeeks() != null ?
                        zone.getWeeks().stream().map(Week::getNumber).collect(Collectors.toList()) : "[]").append("\n");
                sb.append(isLastZone ? "    " : "│   ").append("├── Level: ").append(zone.getComplexityLevel()).append("\n");
                sb.append(isLastZone ? "    " : "│   ").append("└── Skills: ").append(zone.getLearningGoal() != null ?
                        zone.getLearningGoal().replace("\n", " ") : "").append("\n");

                // Добавляем детальную информацию о неделях
                if (zone.getWeeks() != null && !zone.getWeeks().isEmpty()) {
                    for (int j = 0; j < zone.getWeeks().size(); j++) {
                        Week week = zone.getWeeks().get(j);
                        boolean isLastWeek = j == zone.getWeeks().size() - 1;

                        sb.append(isLastZone ? "    " : "│   ");
                        sb.append(isLastWeek ? "    └── " : "    ├── ");
                        sb.append("Week ").append(week.getNumber()).append(":\n");

                        // Goal
                        sb.append(isLastZone ? "    " : "│   ");
                        sb.append(isLastWeek ? "        ├── " : "    │   ├── ");
                        sb.append("Goal: ").append(week.getGoal() != null ? week.getGoal() : "No goal").append("\n");

                        // Tasks
                        if (week.getTasks() != null && !week.getTasks().isEmpty()) {
                            sb.append(isLastZone ? "    " : "│   ");
                            sb.append(isLastWeek ? "        └── " : "    │   └── ");
                            sb.append("Tasks: ").append(week.getTasks().size()).append("\n");

                            for (int k = 0; k < week.getTasks().size(); k++) {
                                Task task = week.getTasks().get(k);
                                boolean isLastTask = k == week.getTasks().size() - 1;

                                sb.append(isLastZone ? "    " : "│   ");
                                sb.append(isLastWeek ? "            " : "    │       ");
                                sb.append(isLastTask ? "└── " : "├── ");
                                sb.append("Task ").append(k + 1).append(": ")
                                        .append(task.getDescription() != null ? task.getDescription() : "No description").append("\n");

                                // URLs для каждой задачи
                                if (task.getUrls() != null && !task.getUrls().isEmpty()) {
                                    for (int m = 0; m < task.getUrls().size(); m++) {
                                        String url = task.getUrls().get(m);
                                        boolean isLastUrl = m == task.getUrls().size() - 1;

                                        sb.append(isLastZone ? "    " : "│   ");
                                        sb.append(isLastWeek ? "            " : "    │       ");
                                        sb.append(isLastTask ? "    " : "│   ");
                                        sb.append(isLastUrl ? "└── " : "├── ");
                                        sb.append("RESOURSES ").append(m + 1).append(": ").append(url).append("\n");
                                    }
                                } else {
                                    sb.append(isLastZone ? "    " : "│   ");
                                    sb.append(isLastWeek ? "            " : "    │       ");
                                    sb.append(isLastTask ? "    " : "│   ");
                                    sb.append("└── URLs: No URLs provided\n");
                                }
                            }
                        } else {
                            sb.append(isLastZone ? "    " : "│   ");
                            sb.append(isLastWeek ? "        └── " : "    │   └── ");
                            sb.append("Tasks: No tasks\n");
                        }
                    }
                }

                // Добавляем разделитель между зонами (кроме последней)
                if (!isLastZone) {
                    sb.append("│\n");
                }
            }
        }

        return sb.toString();
    }

}
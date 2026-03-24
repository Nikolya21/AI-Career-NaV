package com.aicareer.core.model.courseModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    private Long id;
    private Long weekId;
    private String description;
    private List<String> urls;
    private Instant createdAt;

    public void updateTimestamps() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}

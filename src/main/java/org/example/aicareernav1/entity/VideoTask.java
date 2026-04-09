package org.example.aicareernav1.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.aicareernav1.enums.VideoStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VideoTask {

  @Id
  @ToString.Include
  @EqualsAndHashCode.Include
  @Column(name = "task_id", length = 36)
  private String taskId; // UUID из Python-сервиса

  @Column(name = "topic_name", columnDefinition = "TEXT", nullable = false)
  private String topic;

  @Enumerated(EnumType.STRING)
  @Column(name = "task_status", nullable = false)
  private VideoStatus status; // Используем Enum вместо String для надежности

  @Column(name = "video_url", columnDefinition = "TEXT")
  private String videoUrl;

  @Builder.Default
  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();
}

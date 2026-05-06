package org.example.aicareernav1.entity.dynamicRoadmapEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Запрещаем печатать всё подряд
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Сравниваем только по ID
public class CheckpointContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include // Разрешаем печатать ID в логах
    @EqualsAndHashCode.Include // Объект равен другому, если их ID совпадают
    private Long id;

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    public String summary = "Пока нет пройденного материала...";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    public String shortContext = "Контекста пока нет...";

    @OneToOne
    @JoinColumn(name = "checkpoint_id")
    @JsonBackReference // "Обратная" сторона, которую Jackson должен игнорировать
    private Checkpoint checkpoint;
}

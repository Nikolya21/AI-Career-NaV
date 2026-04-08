package org.example.aicareernav1.model.dataBaseQuestion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class QuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text", unique = true, nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "difficulty", nullable = false)
    private String difficulty;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
      name = "tags_questions",
      joinColumns = @JoinColumn(name = "question_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public QuestionEntity(String text, String difficulty) {
        this.text = text;
        this.difficulty = difficulty;
    }
}

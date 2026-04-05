package org.example.aicareernav1.model.dataBaseQuestion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name_lang", unique = true, nullable = false)
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}

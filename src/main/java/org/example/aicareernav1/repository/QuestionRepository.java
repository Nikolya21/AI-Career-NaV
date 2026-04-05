package org.example.aicareernav1.repository;

import org.example.aicareernav1.model.dataBaseQuestion.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    boolean existsByText(String text);
}

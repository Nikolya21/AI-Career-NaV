package org.example.aicareernav1.repository;

import org.example.aicareernav1.model.dataBaseQuestion.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {
    boolean existsByText(String text);
    @Query("SELECT q FROM QuestionEntity q JOIN q.tags t WHERE LOWER(t.name) = LOWER(:tagName) AND q.difficulty = :diff")
    List<QuestionEntity> findAllByTagNameAndDifficulty(@Param("tagName") String tagName, @Param("diff") String diff);
}

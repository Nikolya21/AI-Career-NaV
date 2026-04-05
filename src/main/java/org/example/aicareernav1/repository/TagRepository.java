package org.example.aicareernav1.repository;

import org.example.aicareernav1.model.dataBaseQuestion.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}

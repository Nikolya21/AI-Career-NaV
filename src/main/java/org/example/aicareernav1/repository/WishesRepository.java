package org.example.aicareernav1.repository;

import org.example.aicareernav1.model.user.UserWishes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishesRepository extends JpaRepository<UserWishes, Long> {

  Optional<UserWishes> findByUserId(Long userId);
  Optional<UserWishes> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
  boolean existsByUserId(Long userId);
  void deleteByUserId(Long userId);
  boolean existsByIdAndUserId(Long id, Long userId);
}

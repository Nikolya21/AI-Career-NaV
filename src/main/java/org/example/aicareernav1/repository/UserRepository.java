package org.example.aicareernav1.repository;

import org.example.aicareernav1.model.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.testResult = :testResult WHERE u.id = :userId")
  void updateTestResult(@Param("userId") Long userId, @Param("testResult") String testResult);
}

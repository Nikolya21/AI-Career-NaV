package org.example.aicareernav1.repository.user.jpa;

import org.example.aicareernav1.model.user.entity.CVDataEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CVDataJpaRepository extends JpaRepository<CVDataEntity, Long> {
  Optional<CVDataEntity> findByUser_Id(Long userId);
}
package org.example.aicareernav1.service.user.mapper;

import org.example.aicareernav1.model.user.UserSkills;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.model.user.entity.UserSkillsEntity;

public class UserSkillsMapper {
  private UserSkillsMapper() {
    /* This utility class should not be instantiated */
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }


  public static UserSkills toModel(UserSkillsEntity entity) {
    if (entity == null) return null;
    return UserSkills.builder()
        .id(entity.getId())
        .userId(entity.getUser().getId())
        .fullCompliancePercentage(entity.getFullCompliancePercentage())
        .skillGaps(entity.getSkillGaps())
        .calculatedAt(entity.getCalculatedAt())
        .build();
  }

  public static UserSkillsEntity toEntity(UserSkills model, UserEntity userEntity) {
    if (model == null) return null;
    return UserSkillsEntity.builder()
        .id(model.getId())
        .user(userEntity)
        .fullCompliancePercentage(model.getFullCompliancePercentage())
        .skillGaps(model.getSkillGaps())
        .calculatedAt(model.getCalculatedAt())
        .build();
  }
}
package org.example.aicareernav1.service;

import lombok.RequiredArgsConstructor;
import org.example.aicareernav1.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  public String getVacancyByEmail(String email) {
    return userRepository.findByEmail(email)
      .map(UserEntity::getVacancyNow)
      .orElse("Вакансия не заполнена");
  }
}

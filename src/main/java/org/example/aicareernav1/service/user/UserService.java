package org.example.aicareernav1.service.user;

import org.example.aicareernav1.dto.user.*;
import org.example.aicareernav1.model.user.CVData;
import org.example.aicareernav1.model.user.User;
import org.example.aicareernav1.model.user.UserPreferences;
import org.example.aicareernav1.model.user.UserSkills;
import org.example.aicareernav1.service.user.model.AuthenticationResult;
import org.example.aicareernav1.service.user.model.RegistrationResult;
import org.example.aicareernav1.service.user.model.UpdateResult;
import java.io.File;
import java.util.List;

public interface UserService {
  // === АУТЕНТИФИКАЦИЯ И РЕГИСТРАЦИЯ ===
  RegistrationResult registerUser(UserRegistrationDto registrationDto);
  AuthenticationResult authenticateUser(LoginRequestDto loginRequest);
  boolean isEmailAvailable(String email);

  User getUserProfile(Long userId);
  List<User> getAllUsers();

  UserPreferences getUserPreferences(Long userId);

  UpdateResult updateUserPreferencesInfo(Long userId, String newInfoAboutPerson);

  boolean hasUserPreferences(Long userId);

  CVData getCVDataByUserId(Long userId);
  UserPreferences saveUserPreferences(UserPreferences preferences, Long userId);

  UpdateResult updateVacancy(String vacancy, Long userId);
  UpdateResult updateRoadmap(Long roadmapId, Long userId);

  UpdateResult uploadCV(File cv, Long userId);
  UpdateResult updateSkills(UserSkills skills, Long userId);
}
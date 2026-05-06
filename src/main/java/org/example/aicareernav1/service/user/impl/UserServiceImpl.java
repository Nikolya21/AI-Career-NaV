package org.example.aicareernav1.service.user.impl;

import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.example.aicareernav1.dto.user.*;
import org.example.aicareernav1.model.user.*;
import org.example.aicareernav1.model.user.entity.*;
import org.example.aicareernav1.service.user.UserService;
import org.example.aicareernav1.service.user.model.AuthenticationResult;
import org.example.aicareernav1.service.user.model.RegistrationResult;
import org.example.aicareernav1.service.user.model.UpdateResult;
import org.example.aicareernav1.service.user.util.PasswordEncoder;
import org.example.aicareernav1.validator.user.AuthenticationValidator;
import org.example.aicareernav1.validator.user.RegistrationValidator;
import org.example.aicareernav1.repository.user.jpa.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

  private final UserJpaRepository userJpaRepository;
  private final CVDataJpaRepository cvDataJpaRepository;
  private final UserSkillsJpaRepository userSkillsJpaRepository;
  private final UserPreferencesJpaRepository userPreferencesJpaRepository;

  // ========== Вспомогательные методы конвертации ==========

  public UserEntity getUserByRoadmapId(Long roadmapId) {
    return userJpaRepository.findByRoadmapId(roadmapId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  @Transactional
  public UserEntity saveUser(UserEntity user) {
    return userJpaRepository.save(user);
  }

  public UserEntity getUserById(Long userId) {
    return userJpaRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
  }

  private User toUser(UserEntity entity) {
    if (entity == null) return null;
    return User.builder()
            .id(entity.getId())
            .name(entity.getName())
            .email(entity.getEmail())
            .passwordHash(entity.getPasswordHash())
            .vacancyNow(entity.getVacancyNow())
            .roadmapId(entity.getRoadmapId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
  }

  private UserEntity toUserEntity(User user) {
    if (user == null) return null;
    return UserEntity.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .passwordHash(user.getPasswordHash())
            .vacancyNow(user.getVacancyNow())
            .roadmapId(user.getRoadmapId())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
  }

  private CVData toCVData(CVDataEntity entity) {
    if (entity == null) return null;
    return CVData.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .file(null)
            .information(entity.getInformation())
            .uploadedAt(entity.getUploadedAt())
            .build();
  }

  private CVDataEntity toCVDataEntity(CVData data, UserEntity user) {
    if (data == null) return null;
    return CVDataEntity.builder()
            .id(data.getId())
            .user(user)
            .information(data.getInformation())
            .uploadedAt(data.getUploadedAt())
            .build();
  }

  private UserPreferences toUserPreferences(UserPreferencesEntity entity) {
    if (entity == null) return null;
    return UserPreferences.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .infoAboutPerson(entity.getInfoAboutPerson())
            .build();
  }

  private UserPreferencesEntity toUserPreferencesEntity(UserPreferences preferences, UserEntity user) {
    if (preferences == null) return null;
    return UserPreferencesEntity.builder()
            .id(preferences.getId())
            .user(user)
            .infoAboutPerson(preferences.getInfoAboutPerson())
            .build();
  }

  private UserSkills toUserSkills(UserSkillsEntity entity) {
    if (entity == null) return null;
    return UserSkills.builder()
            .id(entity.getId())
            .userId(entity.getUser().getId())
            .fullCompliancePercentage(entity.getFullCompliancePercentage())
            .skillGaps(entity.getSkillGaps())
            .calculatedAt(entity.getCalculatedAt())
            .build();
  }

  private UserSkillsEntity toUserSkillsEntity(UserSkills skills, UserEntity user) {
    if (skills == null) return null;
    return UserSkillsEntity.builder()
            .id(skills.getId())
            .user(user)
            .fullCompliancePercentage(skills.getFullCompliancePercentage())
            .skillGaps(skills.getSkillGaps())
            .calculatedAt(skills.getCalculatedAt())
            .build();
  }

  // ========== Методы интерфейса ==========

  @Override
  public RegistrationResult registerUser(UserRegistrationDto registrationDto) {
    List<String> validationErrors = RegistrationValidator.validate(
            registrationDto,
            this::isEmailAvailable
    );

    if (!validationErrors.isEmpty()) {
      return RegistrationResult.error(validationErrors);
    }

    log.info("Registering user with email: {}", registrationDto.getEmail());

    try {
      UserEntity userEntity = UserEntity.builder()
              .name(registrationDto.getName())
              .email(registrationDto.getEmail())
              .passwordHash(PasswordEncoder.encode(registrationDto.getPassword()))
              .vacancyNow(null)
              .roadmapId(null)
              .build();

      UserEntity savedEntity = userJpaRepository.save(userEntity);
      log.info("User registered successfully with ID: {}", savedEntity.getId());

      return RegistrationResult.success(savedEntity);

    } catch (Exception e) {
      log.error("Error during registration", e);
      return RegistrationResult.error(List.of("Системная ошибка: " + e.getMessage()));
    }
  }

  @Override
  public CVData getCVDataByUserId(Long userId) {
    CVDataEntity entity = cvDataJpaRepository.findByUser_Id(userId)
            .orElseThrow(() -> new IllegalArgumentException("CV not found for user " + userId));
    return toCVData(entity);
  }

  @Override
  public UserPreferences saveUserPreferences(UserPreferences preferences, Long userId) {
    UserEntity user = userJpaRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    UserPreferencesEntity entity = toUserPreferencesEntity(preferences, user);
    UserPreferencesEntity saved = userPreferencesJpaRepository.save(entity);
    return toUserPreferences(saved);
  }

  @Override
  public AuthenticationResult authenticateUser(LoginRequestDto loginRequest) {
    List<String> validationErrors = AuthenticationValidator.validate(loginRequest);
    if (!validationErrors.isEmpty()) {
      return AuthenticationResult.error(validationErrors);
    }

    log.info("Authentication attempt for email: {}", loginRequest.getEmail());

    try {
      Optional<UserEntity> userOpt = userJpaRepository.findByEmail(loginRequest.getEmail());
      if (userOpt.isEmpty()) {
        return AuthenticationResult.error("Неверный email или пароль");
      }

      UserEntity userEntity = userOpt.get();
      if (!PasswordEncoder.matches(loginRequest.getPassword(), userEntity.getPasswordHash())) {
        return AuthenticationResult.error("Неверный email или пароль");
      }

      log.info("User authenticated successfully: {}", userEntity.getEmail());
      return AuthenticationResult.success(toUser(userEntity));

    } catch (Exception e) {
      log.error("Authentication error", e);
      return AuthenticationResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public User getUserProfile(Long userId) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("Invalid user ID");
    }

    UserEntity userEntity = userJpaRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    // Возвращаем только нужные поля
    User profile = new User();
    profile.setId(userEntity.getId());
    profile.setEmail(userEntity.getEmail());
    profile.setName(userEntity.getName());
    profile.setVacancyNow(userEntity.getVacancyNow());
    profile.setRoadmapId(userEntity.getRoadmapId());
    profile.setCreatedAt(userEntity.getCreatedAt());
    return profile;
  }

  @Override
  public List<User> getAllUsers() {
    return userJpaRepository.findAll().stream()
            .map(this::toUser)
            .collect(Collectors.toList());
  }

  @Override
  public boolean isEmailAvailable(String email) {
    return !userJpaRepository.existsByEmail(email);
  }

  @Override
  public UpdateResult updateVacancy(String vacancy, Long userId) {
    if (vacancy == null || vacancy.trim().isEmpty()) {
      return UpdateResult.error("Вакансия не может быть пустой");
    }
    if (vacancy.length() > 255) {
      return UpdateResult.error("Название вакансии слишком длинное");
    }
    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }

    log.info("Updating vacancy for user: {}", userId);

    try {
      UserEntity userEntity = userJpaRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

      userEntity.setVacancyNow(vacancy.trim());
      userJpaRepository.save(userEntity);
      log.info("Vacancy updated to: {}", vacancy);
      return UpdateResult.success();

    } catch (Exception e) {
      log.error("Error updating vacancy", e);
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public UpdateResult updateRoadmap(Long roadmapId, Long userId) {
    if (roadmapId == null || roadmapId <= 0) {
      return UpdateResult.error("Неверный ID дорожной карты");
    }
    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }

    log.info("Updating roadmap for user: {}", userId);

    try {
      UserEntity userEntity = userJpaRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

      userEntity.setRoadmapId(roadmapId);
      userJpaRepository.save(userEntity);
      log.info("Roadmap updated to ID: {}", roadmapId);
      return UpdateResult.success();

    } catch (Exception e) {
      log.error("Error updating roadmap", e);
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public UpdateResult updateSkills(UserSkills skills, Long userId) {
    if (skills == null) {
      return UpdateResult.error("Навыки не могут быть пустыми");
    }
    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }
    if (skills.getFullCompliancePercentage() < 0 || skills.getFullCompliancePercentage() > 100) {
      return UpdateResult.error("Общий процент соответствия должен быть от 0 до 100");
    }

    log.info("Updating skills for user: {}", userId);

    try {
      UserEntity userEntity = userJpaRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

      UserSkillsEntity skillsEntity = toUserSkillsEntity(skills, userEntity);
      skillsEntity.setCalculatedAt(skills.getCalculatedAt()); // уже установлено
      userSkillsJpaRepository.save(skillsEntity);

      log.info("Skills updated for user ID: {}", userId);
      return UpdateResult.success();

    } catch (Exception e) {
      log.error("Error updating skills", e);
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public UpdateResult uploadCV(File cvFile, Long userId) {
    if (cvFile == null) {
      return UpdateResult.error("Файл не может быть пустым");
    }

    String fileName = cvFile.getName().toLowerCase();
    if (!fileName.endsWith(".pdf") && !fileName.endsWith(".docx")) {
      return UpdateResult.error("Файл должен быть в формате PDF или DOCX");
    }

    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }

    log.info("Uploading CV for user: {}", userId);

    try {
      UserEntity userEntity = userJpaRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

      String extractedText = extractTextFromFile(cvFile);

      CVDataEntity cvDataEntity = CVDataEntity.builder()
              .user(userEntity)
              .information(extractedText)
              .build();

      cvDataJpaRepository.save(cvDataEntity);
      log.info("CV uploaded successfully for user ID: {}", userId);
      return UpdateResult.success();

    } catch (IOException e) {
      log.error("IO error reading CV file", e);
      return UpdateResult.error("Ошибка чтения файла: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error uploading CV", e);
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public UserPreferences getUserPreferences(Long userId) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("Неверный ID пользователя");
    }

    log.debug("Getting user preferences for user: {}", userId);

    try {
      return userPreferencesJpaRepository.findByUser_Id(userId)
              .map(this::toUserPreferences)
              .orElse(null);
    } catch (Exception e) {
      throw new RuntimeException("Системная ошибка при получении настроек: " + e.getMessage());
    }
  }

  @Override
  public UpdateResult updateUserPreferencesInfo(Long userId, String newInfoAboutPerson) {
    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }
    if (newInfoAboutPerson == null || newInfoAboutPerson.trim().isEmpty()) {
      return UpdateResult.error("Новая информация о пользователе не может быть пустой");
    }

    log.info("Updating user preferences info for user: {}", userId);

    try {
      UserEntity userEntity = userJpaRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

      UserPreferencesEntity preferencesEntity = userPreferencesJpaRepository.findByUser_Id(userId)
              .orElseGet(() -> UserPreferencesEntity.builder().user(userEntity).build());

      preferencesEntity.setInfoAboutPerson(newInfoAboutPerson.trim());
      userPreferencesJpaRepository.save(preferencesEntity);

      log.info("User preferences info updated for user ID: {}", userId);
      return UpdateResult.success();

    } catch (Exception e) {
      log.error("Error updating user preferences", e);
      return UpdateResult.error("Системная ошибка при обновлении настроек: " + e.getMessage());
    }
  }

  @Override
  public boolean hasUserPreferences(Long userId) {
    if (userId == null || userId <= 0) {
      return false;
    }

    try {
      return userPreferencesJpaRepository.existsByUser_Id(userId);
    } catch (Exception e) {
      log.error("Error checking user preferences existence", e);
      return false;
    }
  }

  // ========== Вспомогательные методы ==========

  private String extractTextFromFile(File file) throws IOException {
    String fileName = file.getName().toLowerCase();
    if (fileName.endsWith(".pdf")) {
      return extractTextFromPdf(file);
    } else if (fileName.endsWith(".docx")) {
      return extractTextFromDocx(file);
    } else {
      throw new IllegalArgumentException("Unsupported file format: " + fileName);
    }
  }

  private String extractTextFromPdf(File file) throws IOException {
    try (PDDocument document = PDDocument.load(file)) {
      PDFTextStripper stripper = new PDFTextStripper();
      return stripper.getText(document);
    }
  }

  private String extractTextFromDocx(File file) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (FileInputStream fis = new FileInputStream(file);
         XWPFDocument document = new XWPFDocument(fis)) {
      for (XWPFParagraph para : document.getParagraphs()) {
        sb.append(para.getText()).append("\n");
      }
    }
    return sb.toString();
  }
}
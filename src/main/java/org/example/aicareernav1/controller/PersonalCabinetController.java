package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;

@Slf4j
@Controller
@RequestMapping("/personal-cabinet")
@RequiredArgsConstructor
public class PersonalCabinetController {

  private final UserService userService;

  @GetMapping("/{userId}")
  public String showPersonalCabinet(@PathVariable("userId") Long userId, HttpSession session, Model model) {
    // Проверка авторизации
    if (session.getAttribute("authenticated") == null) {
      log.warn("Доступ запрещен: пользователь не авторизован");
      return "redirect:/login";
    }

    // Для безопасности: проверяем, что ID в URL совпадает с ID в сессии
    Long sessionUserId = (Long) session.getAttribute("userId");
    if (sessionUserId == null || !sessionUserId.equals(userId)) {
      log.error("Попытка доступа к чужому кабинету! Сессия: {}, URL: {}", sessionUserId, userId);
      return "redirect:/login";
    }

    // Данные для отображения (имя берем из сессии)
    model.addAttribute("userName", session.getAttribute("userName"));
    model.addAttribute("userId", session.getAttribute("userId"));
    model.addAttribute("registrationDate", session.getAttribute("registrationDate"));

    return "personal-cabinet";
  }

  /**
   * Метод для загрузки резюме.
   * Путь: /personal-cabinet/upload
   */
  @PostMapping("/upload")
  public String handleResumeUpload(@RequestParam("resumeFile") MultipartFile multipartFile,
      HttpSession session) {
    Long userId = (Long) session.getAttribute("userId");

    if (userId == null) {
      log.error("Ошибка загрузки: userId не найден в сессии");
      return "redirect:/login";
    }

    if (multipartFile.isEmpty()) {
      session.setAttribute("uploadError", "Файл не выбран или пуст");
      return "redirect:/personal-cabinet";
    }

    File tempFile = null;
    try {
      // 1. Создаем временный файл с сохранением расширения
      String originalFilename = multipartFile.getOriginalFilename();
      String suffix = ".tmp";
      if (originalFilename != null && originalFilename.contains(".")) {
        suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
      }

      tempFile = File.createTempFile("cv_upload_", suffix);
      multipartFile.transferTo(tempFile);

      log.info("Вызов uploadCV для пользователя ID: {} (Файл: {})", userId, originalFilename);

      // 2. Вызываем твой метод из UserService
      userService.uploadCV(tempFile, userId);

      // 3. Сохраняем результат в сессию для отображения плашки на фронте
      session.setAttribute("resumeUploaded", true);
      session.setAttribute("resumeFilename", originalFilename);
      session.setAttribute("resumeUploadDate", new Date());
      session.setAttribute("uploadSuccess", "Резюме успешно загружено и проанализировано!");
      session.removeAttribute("uploadError");

    } catch (Exception e) {
      log.error("Критическая ошибка при загрузке резюме: ", e);
      session.setAttribute("uploadError", "Ошибка при обработке файла: " + e.getMessage());
      session.removeAttribute("uploadSuccess");
    } finally {
      // 4. Очистка временных данных на сервере
      if (tempFile != null && tempFile.exists()) {
        boolean deleted = tempFile.delete();
        if (!deleted) log.warn("Не удалось удалить временный файл: {}", tempFile.getAbsolutePath());
      }
    }

    return "redirect:/personal-cabinet/" + userId;
  }
}

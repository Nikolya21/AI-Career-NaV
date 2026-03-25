package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequestMapping("/personal-cabinet")
public class PersonalCabinetController {

  private static final String UPLOAD_DIR = "uploads/resumes";

  @GetMapping
  public String showCabinet(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    if (session.getAttribute("registrationDate") == null) {
      session.setAttribute("registrationDate", new Date());
    }

    if (session.getAttribute("userName") == null) {
      String userEmail = (String) session.getAttribute("userEmail");
      if (userEmail != null) {
        session.setAttribute("userName", userEmail.split("@")[0]);
      }
    }

    return "personal-cabinet";
  }

  @PostMapping
  public String uploadResume(@RequestParam("resumeFile") MultipartFile file,
      HttpSession session) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    if (file.isEmpty()) {
      session.setAttribute("uploadError", "Файл не выбран");
      return "redirect:/personal-cabinet";
    }

    String fileName = file.getOriginalFilename();
    if (!isValidFileExtension(fileName)) {
      session.setAttribute("uploadError", "Неподдерживаемый формат файла. Разрешены: PDF, DOC, DOCX, TXT");
      return "redirect:/personal-cabinet";
    }

    if (file.getSize() > 10 * 1024 * 1024) {
      session.setAttribute("uploadError", "Файл слишком большой. Максимальный размер: 10 МБ");
      return "redirect:/personal-cabinet";
    }

    try {
      // Сохраняем файл
      String uploadPath = System.getProperty("java.io.tmpdir") + File.separator + UPLOAD_DIR;
      Path uploadDir = Paths.get(uploadPath);
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }

      Long userId = (Long) session.getAttribute("userId");
      String uniqueFileName = (userId != null ? userId + "_" : "") + System.currentTimeMillis() + "_" + fileName;
      Path targetLocation = uploadDir.resolve(uniqueFileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      // Сохраняем метаданные в сессии
      session.setAttribute("resumeFilename", fileName);
      session.setAttribute("resumeUploadDate", new Date());
      session.setAttribute("resumeUploaded", true);
      session.setAttribute("resumeFilePath", targetLocation.toString());
      session.setAttribute("resumeFileUrl", UPLOAD_DIR + "/" + uniqueFileName);

      session.setAttribute("uploadSuccess", "Резюме успешно загружено!");
    } catch (IOException e) {
      log.error("Ошибка сохранения файла", e);
      session.setAttribute("uploadError", "Ошибка при сохранении файла: " + e.getMessage());
    }

    return "redirect:/personal-cabinet";
  }

  private boolean isValidFileExtension(String fileName) {
    if (fileName == null) return false;
    String lower = fileName.toLowerCase();
    return lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt");
  }
}
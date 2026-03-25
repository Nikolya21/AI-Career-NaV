package org.example.aicareernav1.controller;

import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequestMapping("/upload-document")
public class UploadDocumentController {

  @PostMapping
  public String uploadDocument(@RequestParam("documentFile") MultipartFile file,
      HttpSession session) {
    String uploadError = null;
    if (file.isEmpty()) {
      uploadError = "Файл не выбран.";
    } else {
      String contentType = file.getContentType();
      if (!"application/pdf".equals(contentType) &&
          !"application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
        uploadError = "Разрешены только PDF или DOCX.";
      } else {
        String fileName = file.getOriginalFilename();
        session.setAttribute("uploadedDocumentName", fileName);
        session.setAttribute("documentUploaded", true);
        // TODO: сохранить файл (на диск / в БД)
        log.info("Документ загружен: {}", fileName);
      }
    }

    if (uploadError != null) {
      return "redirect:/personal-cabinet?error=" + URLEncoder.encode(uploadError, StandardCharsets.UTF_8);
    } else {
      return "redirect:/personal-cabinet";
    }
  }
}
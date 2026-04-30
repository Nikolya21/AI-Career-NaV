package org.example.aicareernav1.controller;

import org.example.aicareernav1.service.email.AnalysisMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/results")
public class AnalysisEmailController {
    @Autowired
    private AnalysisMailService mailService;

    @PostMapping("/send-to-email")
    public ResponseEntity<String> sendResult(@RequestParam String email) {
        try {
            mailService.sendAnalysisByEmail(email);
            return ResponseEntity.ok("Результат успешно отправлен на " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при отправке: " + e.getMessage());
        }
    }
}

package org.example.aicareernav1.service.email;

import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.model.user.entity.UserEntity;
import org.example.aicareernav1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalysisMailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    public void sendAnalysisByEmail(String email) {
        log.info("Попытка отправить письмо на адрес: {}", email); // Лог 1

        try {
            UserEntity user = userRepository.findByEmail(email)
              .orElseThrow(() -> new RuntimeException("Пользователь не найден в БД"));

            String analysis = user.getTestAnalysis();
            log.info("Данные из БД получены. Длина текста анализа: {}",
              (analysis != null ? analysis.length() : "null")); // Лог 2

            if (analysis == null || analysis.isEmpty()) {
                log.warn("Анализ пуст, отправка отменена.");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("ai.career.nav.info@mail.ru");
            message.setTo(email);
            message.setSubject("Результат теста AI Career Nav");
            message.setText(analysis);

            log.info("Отправка сообщения через SMTP..."); // Лог 3
            mailSender.send(message);
            log.info("Письмо успешно ушло на сервер Mail.ru!"); // Лог 4

        } catch (Exception e) {
            log.error("КРИТИЧЕСКАЯ ОШИБКА ПРИ ОТПРАВКЕ: ", e); // Выведет всю ошибку
        }
    }
}


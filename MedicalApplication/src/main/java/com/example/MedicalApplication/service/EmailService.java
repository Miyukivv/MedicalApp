package com.example.MedicalApplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("medicalappemailtest@gmail.com");
        message.setTo("medicalappemailtest@gmail.com");
        message.setSubject("Testowanie, czy działa");
        message.setText("Jeśli przyszło, to działa!");

        mailSender.send(message);
    }
}

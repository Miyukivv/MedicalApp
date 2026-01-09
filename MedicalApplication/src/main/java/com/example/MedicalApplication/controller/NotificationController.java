package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.service.EmailService;
import com.example.MedicalApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;



    @RestController
    @RequiredArgsConstructor
    public class NotificationController {

        private final EmailService emailService;

        @RequestMapping("/send-email")
        public String sendEmail() {
            try {
                emailService.sendTestEmail();
                return "Email sent successfully";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }
    }




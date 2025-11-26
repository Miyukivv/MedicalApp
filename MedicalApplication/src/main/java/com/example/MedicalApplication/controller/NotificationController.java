package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.service.EmailService;
import com.example.MedicalApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/user/{userId}/test-email")
    @ResponseStatus(HttpStatus.OK)
    public String sendTestEmail(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("User has no email set");
        }

        emailService.sendMedicationReminder(
                user.getEmail(),
                "Testowe powiadomienie o leku",
                "Cześć " + user.getName() + ", to jest testowe powiadomienie o leku."
        );

        return "Notification email sent to " + user.getEmail();
    }
}
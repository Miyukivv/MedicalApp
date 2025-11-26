package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.Reminder;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.service.MedicationService;
import com.example.MedicalApplication.service.ReminderService;
import com.example.MedicalApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final UserService userService;
    private final MedicationService medicationService;

    @PostMapping("/user/{userId}/medication/{medicationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Reminder createReminder(
            @PathVariable Long userId,
            @PathVariable Long medicationId,
            @RequestBody Map<String, Object> body
    ) {
        User patient = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Medication medication = medicationService.getMedicationsForUser(patient).stream()
                .filter(m -> m.getId().equals(medicationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Medication not found for this user"));

        String doseDateTimeStr = (String) body.get("doseDateTime"); // np. "2025-01-10T15:00:00"
        int minutesBefore = (int) body.getOrDefault("minutesBefore", 30);
        boolean lockedByDoctor = (boolean) body.getOrDefault("lockedByDoctor", false);

        LocalDateTime doseDateTime = LocalDateTime.parse(doseDateTimeStr);

        return reminderService.createReminder(
                patient,
                medication,
                doseDateTime,
                minutesBefore,
                lockedByDoctor
        );
    }

    @GetMapping("/user/{userId}")
    public List<Reminder> getActiveReminders(@PathVariable Long userId) {
        User patient = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reminderService.getActiveRemindersForPatient(patient);
    }

    @PostMapping("/user/{userId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateForUser(@PathVariable Long userId) {
        User patient = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        reminderService.deactivateAllForPatient(patient);
    }
}

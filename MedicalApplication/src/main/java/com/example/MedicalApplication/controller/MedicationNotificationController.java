package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.MedicationStatus;
import com.example.MedicalApplication.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationNotificationController {

    private final MedicationRepository medicationRepository;

    @GetMapping("/taken")
    @Transactional
    public String markTaken(@RequestParam("token") String token) {
        Medication m = medicationRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // potwierdzamy tylko dla bieżącej "pendingDoseDate"
        if (m.getTakenAt() == null) {
            m.setTakenAt(LocalDateTime.now());
            m.setStatus(MedicationStatus.TAKEN);
            medicationRepository.save(m);
        }

        return "Dzięki! Zapisano jako przyjęte.";
    }
}





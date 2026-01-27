package com.example.MedicalApplication.service;

import com.example.MedicalApplication.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TherapyCleanupScheduler {

    private final MedicationRepository medicationRepository;

    @Transactional
    @Scheduled(cron = "0 10 0 * * *")
    public void cleanup() {
        LocalDate today = LocalDate.now();
        var ended = medicationRepository.findByCreatedByDoctorTrueAndTherapyEndDateBefore(today);
        medicationRepository.deleteAll(ended);
    }
}

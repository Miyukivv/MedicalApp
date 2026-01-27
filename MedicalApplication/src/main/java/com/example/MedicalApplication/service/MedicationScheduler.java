package com.example.MedicalApplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MedicationScheduler {

    private final MedicationJobService medicationJobService;

    @Scheduled(fixedRate = 60_000)
    public void run() {
        medicationJobService.tick();
    }
}


package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.service.MedicationService;
import com.example.MedicalApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientMedicationController {
    private final UserService userService;
    private final MedicationService medicationService;

    //pacjent dodaje np. witaminy
    @PostMapping("/{patientId}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    public Medication addMedicationByPatient(@PathVariable Long patientId,
                                             @RequestBody Medication medRequest) {
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return medicationService.addMedicationByPatient(patient, medRequest);
    }

    @GetMapping("/{patientId}/medications")
    public List<Medication> getMedications(@PathVariable Long patientId) {
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return medicationService.getMedicationsForUser(patient);
    }

    @PostMapping("/{patientId}/medications/{medId}/taken")
    public Medication markTaken(@PathVariable Long patientId,
                                @PathVariable Long medId) {
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return medicationService.markAsTaken(patient, medId);
    }

    public ResponseEntity<?> deleteOwnMedication(@PathVariable Long patientId,
                                                 @PathVariable Long medicationId) {
        medicationService.deleteMedicationIfPatientCreated(patientId, medicationId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/medications/delete/{id}")
    public String deleteMedication(@PathVariable Long id, @AuthenticationPrincipal User user) {
        // Sprawdzamy, czy pacjent może usunąć dany lek
        medicationService.deleteMedicationIfPatientCreated(user.getId(), id);
        return "redirect:/medications";  // Przekierowanie po usunięciu leku
    }



    //pacjent aktualizuje TYLKO swoje własne leki (nie od lekarza)
    @PutMapping("/{patientId}/medications/{medId}")
    public Medication updateMedication(@PathVariable Long patientId,
                                       @PathVariable Long medId,
                                       @RequestBody Medication update) {
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return medicationService.updateMedicationByPatient(patient, medId, update);
    }
}


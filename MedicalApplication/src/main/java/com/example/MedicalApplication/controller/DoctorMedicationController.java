package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.service.MedicationService;
import com.example.MedicalApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorMedicationController {

    private final UserService userService;
    private final MedicationService medicationService;

    @PostMapping("/{doctorId}/patients/{patientId}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    public Medication prescribeMedication(@PathVariable Long doctorId,
                                          @PathVariable Long patientId,
                                          @RequestBody Medication medRequest) {
        User doctor = userService.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        User patient = userService.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Medication medication = medicationService.addMedicationByDoctor(doctor, patient, medRequest);
        return medication;
    }

    @GetMapping("/{doctorId}/medications")
    public List<Medication> getMedicationsByDoctor(@PathVariable Long doctorId) {
        User doctor = userService.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        List<Medication> medications = medicationService.getMedicationsByDoctor(doctor);
        return medications;
    }

    // Usuwanie leku tylko przez lekarza, który go przypisał

    @DeleteMapping("/{doctorId}/medications/{medicationId}")
    public String deleteMedication(@PathVariable Long doctorId, @PathVariable Long medicationId) {
        User doctor = userService.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Lekarz nie został znaleziony"));

        Medication medication = medicationService.getMedicationById(medicationId);

        // Sprawdzamy, czy lekarz jest właścicielem leku
        if (!medication.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Możesz usunąć tylko leki, które przypisałeś.");
        }

        // Usuwamy lek
        medicationService.deleteMedication(medicationId);
        return "Lek został usunięty pomyślnie.";
    }

}

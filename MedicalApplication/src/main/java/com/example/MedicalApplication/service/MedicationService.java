package com.example.MedicalApplication.service;

import com.example.MedicalApplication.model.UserRole;
import com.example.MedicalApplication.repository.MedicationRepository;
import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public Medication addMedicationByPatient(User patient, Medication medRequest) {
        if (patient.getRole() != UserRole.PATIENT) {
            throw new RuntimeException("Only patients can add their own medications");
        }

        medRequest.setId(null);
        medRequest.setPatient(patient);
        medRequest.setCreatedByDoctor(false);
        if (medRequest.getStatus() == null) {
            medRequest.setStatus("SCHEDULED");
        }
        return medicationRepository.save(medRequest);
    }
    public Medication addMedicationByDoctor(User doctor, User patient, Medication medRequest) {
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new RuntimeException("Only doctors can prescribe medications");
        }

        medRequest.setId(null);
        medRequest.setPatient(patient);
        medRequest.setCreatedByDoctor(true);
        if (medRequest.getStatus() == null) {
            medRequest.setStatus("SCHEDULED");
        }
        return medicationRepository.save(medRequest);
    }

    public List<Medication> getMedicationsForUser(User patient) {
        return medicationRepository.findByPatient(patient);
    }
    // pacjent może oznaczyć lek jako przyjęty (nawet jeśli lek jest od lekarza)
    public Medication markAsTaken(User patient, Long medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("You can only update your own medications");
        }

        medication.setStatus("TAKEN");
        return medicationRepository.save(medication);
    }

    // pacjent próbuje edytować dawkę/harmonogram
    public Medication updateMedicationByPatient(User patient, Long medId, Medication update) {
        Medication medication = medicationRepository.findById(medId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (!medication.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("You can only edit your own medications");
        }

        if (medication.isCreatedByDoctor()) {
            throw new RuntimeException("You cannot edit medication prescribed by a doctor");
        }

        medication.setName(update.getName());
        medication.setDose(update.getDose());
        medication.setIntakeTime(update.getIntakeTime());

        return medicationRepository.save(medication);
    }

    public List<Medication> getMedicationsByDoctor(User doctor) {
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new RuntimeException("Only doctors can prescribe medications");
        }
        return medicationRepository.findByCreatedByDoctorTrue(); // zwraca leki, które zostały przypisane przez lekarza
    }

}
package com.example.MedicalApplication.service;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.model.UserRole;
import com.example.MedicalApplication.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.MedicalApplication.model.MedicationStatus.SCHEDULED;
import static com.example.MedicalApplication.model.MedicationStatus.TAKEN;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public Medication addMedicationSelf(User owner, Medication medRequest) {
        medRequest.setId(null);
        medRequest.setPatient(owner);
        medRequest.setCreatedByDoctor(false);
        medRequest.setDoctor(null);
        ensureStatus(medRequest);
        return medicationRepository.save(medRequest);
    }


    public Medication addMedicationByPatient(User patient, Medication medRequest) {
        if (patient.getRole() != UserRole.PATIENT) {
            throw new RuntimeException("Only patients can add their own medications");
        }

        medRequest.setId(null);
        medRequest.setPatient(patient);
        medRequest.setCreatedByDoctor(false);
        medRequest.setDoctor(null);
        ensureStatus(medRequest);
        return medicationRepository.save(medRequest);
    }

    public Medication addMedicationByDoctor(User doctor, User patient, Medication medRequest) {
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new RuntimeException("Only doctors can prescribe medications");
        }

        medRequest.setId(null);
        medRequest.setPatient(patient);
        medRequest.setCreatedByDoctor(true);
        medRequest.setDoctor(doctor);
        ensureStatus(medRequest);
        return medicationRepository.save(medRequest);
    }

    public List<Medication> getMedicationsForUser(User patient) {
        return medicationRepository.findByPatientOrderByIntakeTimeAsc(patient);
    }

    public List<Medication> getMedicationsByDoctor(User doctor) {
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new RuntimeException("Only doctors can access prescribed medications");
        }
        return medicationRepository.findByDoctorOrderByIntakeTimeAsc(doctor);
    }

    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
    }

    //Pacjent może oznaczyć lek jako przyjęty (nawet jeśli lek jest od lekarza)
    @Transactional
    public Medication markAsTaken(User patient, Long medicationId) {
        Medication medication = medicationRepository
                .findByIdAndPatientId(medicationId, patient.getId())
                .orElseThrow(() -> new RuntimeException("Medication not found for this patient"));

        medication.setStatus(TAKEN);
        medication.setTakenAt(LocalDateTime.now());
        medication.setConfirmationToken(null);
        return medicationRepository.save(medication);
    }


    public Medication updateMedicationByPatient(User owner, Long medId, Medication update) {
        Medication medication = getMedicationById(medId);

        if (medication.getPatient() == null || !medication.getPatient().getId().equals(owner.getId())) {
            throw new RuntimeException("You can only edit your own medications");
        }

        if (medication.isCreatedByDoctor()) {
            throw new RuntimeException("You cannot edit medication prescribed by a doctor");
        }

        applyUpdate(medication, update);
        return medicationRepository.save(medication);
    }



    //Data na 0:00 aby aktualizować stan wzięcia leku
    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Warsaw")
    public void resetDailyStatuses() {
        medicationRepository.bulkUpdateStatus(TAKEN, SCHEDULED);
    }



    @Transactional
    public void deleteMedicationIfPatientCreated(Long patientId, Long medicationId) {
        Medication m = medicationRepository.findByIdAndPatientId(medicationId, patientId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        if (m.isCreatedByDoctor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Nie można usunąć leku dodanego przez lekarza.");
        }

        medicationRepository.delete(m);
    }



    // ===== helpers =====

    private void ensureStatus(Medication med) {
        if (med.getStatus() == null) med.setStatus(SCHEDULED);
    }

    private void applyUpdate(Medication target, Medication update) {
        target.setName(update.getName());
        target.setDose(update.getDose());
        target.setIntakeTime(update.getIntakeTime());
    }

    public Medication updateMedicationByDoctor(User doctor, Long medId, Medication update) {
        Medication medication = getMedicationById(medId);

        if (!medication.isCreatedByDoctor()) {
            throw new RuntimeException("This medication is not doctor-prescribed");
        }

        if (medication.getDoctor() == null || !medication.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You can only edit medications you prescribed");
        }

        applyUpdate(medication, update);
        medication.setTherapyStartDate(update.getTherapyStartDate()); // Aktualizacja daty rozpoczęcia
        medication.setTherapyEndDate(update.getTherapyEndDate()); // Aktualizacja daty zakończenia
        return medicationRepository.save(medication);
    }

    public void setEndDate(Long medicationId, LocalDate endDate) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));

        medication.setTherapyEndDate(endDate);  // Zakładając, że masz pole therapyEndDate w klasie Medication
        medicationRepository.save(medication);
    }
    public void deleteMedication(Long medicationId) {
        // Sprawdzamy, czy lek istnieje
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));

        // Usuwamy lek
        medicationRepository.delete(medication);
    }


}

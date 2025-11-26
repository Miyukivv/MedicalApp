package com.example.MedicalApplication.repository;
import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByPatient(User patient);
    List<Medication> findByCreatedByDoctorTrue();
}
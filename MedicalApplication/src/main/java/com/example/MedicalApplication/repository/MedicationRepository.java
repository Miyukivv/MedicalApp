package com.example.MedicalApplication.repository;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.MedicationStatus;
import com.example.MedicalApplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findByPatientOrderByIntakeTimeAsc(User patient);

    Optional<Medication> findByIdAndPatientId(Long id, Long patientId);

    List<Medication> findByCreatedByDoctorTrue();


    Optional<Medication> findByConfirmationToken(String token);

    List<Medication> findByCreatedByDoctorTrueAndTherapyEndDateBefore(LocalDate date);

    @Query("""
select m from Medication m
join fetch m.patient p
where (m.therapyEndDate is null or m.therapyEndDate >= :today)
""")
    List<Medication> findActiveForNotificationsFetch(@Param("today") LocalDate today);

    List<Medication> findByDoctorOrderByIntakeTimeAsc(User doctor);


    @Modifying
    @Query("update Medication m set m.status = :to where m.status = :from")
    void bulkUpdateStatus(MedicationStatus from, MedicationStatus to);
}

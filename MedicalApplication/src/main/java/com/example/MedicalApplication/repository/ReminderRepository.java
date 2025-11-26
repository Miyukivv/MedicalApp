package com.example.MedicalApplication.repository;
import com.example.MedicalApplication.model.Reminder;
import com.example.MedicalApplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByPatientAndActiveTrue(User patient);

    List<Reminder> findByDoseDateTimeBetweenAndActiveTrue(
            LocalDateTime from, LocalDateTime to
    );
}

package com.example.MedicalApplication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String dose;

    private LocalTime intakeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicationStatus status;

    @Column(nullable=false)
    private boolean createdByDoctor;

    private LocalDate therapyStartDate;
    private LocalDate therapyEndDate;


    private LocalDate pendingDoseDate;
    @Column(unique = true)
    private String confirmationToken;

    private LocalDateTime reminderEmailSentAt;
    private LocalDateTime takenAt;
    private LocalDateTime followUpSentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private User patient;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;
}

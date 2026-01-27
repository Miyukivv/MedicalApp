package com.example.MedicalApplication.service;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.MedicationStatus;
import com.example.MedicalApplication.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicationJobService {

    //  FILTR TESTOWY
    private static final Set<String> TEST_EMAILS = Set.of(
            "nikodemkaszub1@gmail.com",
            "klementyna.kowalska1@interia.pl"
    );

    @Value("${app.base-url}")
    private String baseUrl;

    private final MedicationRepository medicationRepository;
    private final EmailService emailService;


    @Transactional
    public void tick() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        List<Medication> meds = medicationRepository.findActiveForNotificationsFetch(today);

        for (Medication m : meds) {

            if (m.getTherapyStartDate() != null && today.isBefore(m.getTherapyStartDate())) continue;
            if (m.getTherapyEndDate() != null && today.isAfter(m.getTherapyEndDate())) continue;


            if (m.getPendingDoseDate() == null || !today.equals(m.getPendingDoseDate())) {
                m.setPendingDoseDate(today);
                m.setConfirmationToken(null);
                m.setTakenAt(null);
                m.setFollowUpSentAt(null);
                m.setStatus(MedicationStatus.SCHEDULED);
            }

            // 3) pacjent + filtr testowy
            if (m.getPatient() == null || m.getPatient().getEmail() == null || m.getPatient().getEmail().isBlank()) {
                continue;
            }
            String patientEmail = m.getPatient().getEmail().trim().toLowerCase();
            if (!TEST_EMAILS.contains(patientEmail)) {
                continue;
            }


            if (m.getIntakeTime() == null) continue;

            LocalDateTime doseAt = LocalDateTime.of(today, m.getIntakeTime());
            LocalDateTime notifyAt = doseAt.plusMinutes(5);


            boolean isNotifyWindow = now.isAfter(notifyAt) && now.isBefore(notifyAt.plusMinutes(30));



            if (isNotifyWindow && m.getTakenAt() == null && m.getFollowUpSentAt() == null) {

                if (m.getConfirmationToken() == null) {
                    m.setConfirmationToken(UUID.randomUUID().toString());
                }

                String takenLink = baseUrl + "/api/medications/taken?token=" + m.getConfirmationToken();

                String subject = "Brak potwierdzenia przyjęcia leku: " + m.getName();
                String text =
                        "Cześć " + m.getPatient().getFullName() + ",\n\n" +
                                "Minęło 5 minut od zaplanowanej dawki i nie mamy potwierdzenia przyjęcia.\n\n" +
                                "Lek:\n" +
                                "- Nazwa: " + m.getName() + "\n" +
                                "- Dawka: " + m.getDose() + "\n" +
                                "- Planowana godzina: " + m.getIntakeTime() + "\n\n" +
                                "Jeśli już przyjąłeś/aś – kliknij TAKEN:\n" + takenLink + "\n\n" +
                                "YourMed";

                emailService.sendMedicationReminder(patientEmail, subject, text);


                m.setFollowUpSentAt(now);
                medicationRepository.save(m);
            }
        }
    }
}
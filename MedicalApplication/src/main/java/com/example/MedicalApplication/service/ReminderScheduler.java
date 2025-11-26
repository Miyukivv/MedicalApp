package com.example.MedicalApplication.service;

import com.example.MedicalApplication.model.Reminder;
import com.example.MedicalApplication.service.EmailService;
import com.example.MedicalApplication.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderService reminderService;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60_000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(1);

        List<Reminder> upcoming = reminderService.findRemindersToSend(now, now.plusHours(24));

        for (Reminder reminder : upcoming) {
            LocalDateTime sendTime = reminder.getDoseDateTime()
                    .minusMinutes(reminder.getMinutesBefore());
            if (!sendTime.isBefore(now) && sendTime.isBefore(windowEnd)) {
                var patient = reminder.getPatient();
                var medication = reminder.getMedication();

                if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
                    String subject = "Przypomnienie o leku: " + medication.getName();
                    String text = "Cześć " + patient.getName()
                            + ",\n\nza " + reminder.getMinutesBefore()
                            + " minut powinnaś/powinieneś przyjąć lek: "
                            + medication.getName() + ".\n\nPozdrawiamy,\nMedicalApp";

                    emailService.sendMedicationReminder(patient.getEmail(), subject, text);
                }

            }
        }
    }
}
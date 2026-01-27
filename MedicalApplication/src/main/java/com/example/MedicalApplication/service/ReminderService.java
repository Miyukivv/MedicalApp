// package com.example.MedicalApplication.service;

// import com.example.MedicalApplication.model.Medication;
// import com.example.MedicalApplication.model.Reminder;
// import com.example.MedicalApplication.repository.ReminderRepository;
// import com.example.MedicalApplication.model.User;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;
// // import java.util.List;

// @Service
// @RequiredArgsConstructor
// public class ReminderService {

//     private final ReminderRepository reminderRepository;

//     public Reminder createReminder(User patient, Medication medication,
//                                    LocalDateTime doseDateTime,
//                                    int minutesBefore,
//                                    boolean lockedByDoctor) {

//         Reminder reminder = Reminder.builder()
//                 .patient(patient)
//                 .medication(medication)
//                 .doseDateTime(doseDateTime)
//                 .minutesBefore(minutesBefore)
//                 .lockedByDoctor(lockedByDoctor)
//                 .active(true)
//                 .build();

//         return reminderRepository.save(reminder);
//     }

//     public List<Reminder> getActiveRemindersForPatient(User patient) {
//         return reminderRepository.findByPatientAndActiveTrue(patient);
//     }

//     public void deactivateAllForPatient(User patient) {
//         List<Reminder> reminders = reminderRepository.findByPatientAndActiveTrue(patient);
//         reminders.forEach(r -> r.setActive(false));
//         reminderRepository.saveAll(reminders);
//     }

//     public List<Reminder> findRemindersToSend(LocalDateTime from, LocalDateTime to) {
//         return reminderRepository.findByDoseDateTimeBetweenAndActiveTrue(from, to);
//     }
// }

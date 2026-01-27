package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.DoctorPatient;
import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.model.UserRole;
import com.example.MedicalApplication.repository.DoctorPatientRepository;
import com.example.MedicalApplication.repository.UserRepository;
import com.example.MedicalApplication.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static com.example.MedicalApplication.model.MedicationStatus.SCHEDULED;

@Controller
@RequiredArgsConstructor
public class DoctorPageController {

    private final UserRepository userRepository;
    private final DoctorPatientRepository doctorPatientRepository;
    private final MedicationService medicationService;
   // private final ReminderService reminderService;
    public record PatientRow(Long id, String fullName, String lastVisitDisplay, String lastVisitIso) {}

    @GetMapping("/doctor")
    public String doctorPage(Authentication authentication, Model model) {
        User doctor = userRepository.findByEmail(authentication.getName()).orElseThrow();

        if (doctor.getRole() != UserRole.DOCTOR) {
            return "redirect:/home";
        }

        List<DoctorPatient> links = doctorPatientRepository.findByDoctorId(doctor.getId());
        links.sort(Comparator.comparing((DoctorPatient dp) -> dp.getPatient().getLastName())
                .thenComparing(dp -> dp.getPatient().getFirstName()));

        List<PatientRow> rows = links.stream().map(dp -> {
            User p = dp.getPatient();
            LocalDate lv = dp.getLastVisit();
            return new PatientRow(
                    p.getId(),
                    p.getFullName(),
                    (lv == null ? "-" : lv.toString()),
                    (lv == null ? "" : lv.toString())
            );
        }).toList();

        model.addAttribute("welcomeName", doctor.getFirstName());
        model.addAttribute("patients", rows);

        return "doctor";
    }

    @PostMapping("/doctor/patients/add")
    public String addPatient(Authentication authentication, @RequestParam String email) {
        User doctor = userRepository.findByEmail(authentication.getName()).orElseThrow();
        if (doctor.getRole() != UserRole.DOCTOR) return "redirect:/home";

        String safeEmail = email == null ? "" : email.trim();
        if (safeEmail.isBlank()) return "redirect:/doctor";

        User patient = userRepository.findByEmail(safeEmail).orElse(null);
        if (patient == null) return "redirect:/doctor";

        if (!doctorPatientRepository.existsByDoctorIdAndPatientId(doctor.getId(), patient.getId())) {
            doctorPatientRepository.save(DoctorPatient.builder()
                    .doctor(doctor)
                    .patient(patient)
                    .lastVisit(null)
                    .build());
        }

        return "redirect:/doctor";
    }

    @PostMapping("/doctor/patients/visit")
    public String updateLastVisit(Authentication authentication,
                                  @RequestParam Long patientId,
                                  @RequestParam(required = false) String lastVisit) {

        User doctor = userRepository.findByEmail(authentication.getName()).orElseThrow();
        if (doctor.getRole() != UserRole.DOCTOR) return "redirect:/home";

        DoctorPatient link = doctorPatientRepository
                .findByDoctorIdAndPatientId(doctor.getId(), patientId)
                .orElse(null);

        if (link == null) return "redirect:/doctor";

        LocalDate lv = null;
        if (lastVisit != null && !lastVisit.isBlank()) {
            try { lv = LocalDate.parse(lastVisit); } catch (Exception ignored) {}
        }

        link.setLastVisit(lv);
        doctorPatientRepository.save(link);

        return "redirect:/doctor";
    }

    @GetMapping("/{patientId}/medications")
    public String patientMedsAlias(@PathVariable Long patientId,
                                   @RequestParam(required = false) Long id) {
        if (id != null) {
            return "redirect:/medications?patientId=" + patientId + "&id=" + id;
        }
        return "redirect:/medications?patientId=" + patientId;
    }

    @PostMapping("/{patientId}/medications/new")
    public String addMedicationForPatient(
            Authentication authentication,
            @PathVariable Long patientId,
            @RequestParam String name,
            @RequestParam String dose,
            @RequestParam(required = false) String intakeTime
    ) {
        User doctor = userRepository.findByEmail(authentication.getName()).orElseThrow();
        if (doctor.getRole() != UserRole.DOCTOR) return "redirect:/medications";

        User patient = userRepository.findById(patientId).orElseThrow();

        LocalTime time = (intakeTime == null || intakeTime.isBlank()) ? null : LocalTime.parse(intakeTime);

        Medication m = Medication.builder()
                .name(name == null ? "" : name.trim())
                .dose(dose == null ? "" : dose.trim())
                .intakeTime(time)
                .status(SCHEDULED)
                .createdByDoctor(true)
                .patient(patient)
                .doctor(doctor)
                .build();

        Medication saved = medicationService.addMedicationByDoctor(doctor, patient, m);

        //reminderService.createReminder(patient, saved, LocalDateTime.now(), 30, true);

        return "redirect:/medications?patientId=" + patientId + "&id=" + saved.getId();
    }
}

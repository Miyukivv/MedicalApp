package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.repository.UserRepository;
import com.example.MedicalApplication.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;

import static com.example.MedicalApplication.model.MedicationStatus.SCHEDULED;

@Controller
@RequiredArgsConstructor
public class MedicationPageController {

    private final UserRepository userRepository;
    private final MedicationService medicationService;

    @GetMapping("/medications")
    public String medicationsPage(
            Model model,
            Authentication authentication,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long patientId
    ) {
        var loggedUser = userRepository.findByEmail(authentication.getName()).orElseThrow();

        var targetUser = loggedUser;

        if (patientId != null) {
            if (!isDoctor(loggedUser.getRole())) {
                return "redirect:/medications";
            }
            targetUser = userRepository.findById(patientId).orElseThrow();
        }

        boolean viewingOwn = Objects.equals(targetUser.getId(), loggedUser.getId());

        List<Medication> meds = medicationService.getMedicationsForUser(targetUser);
        meds.sort(Comparator.comparing(Medication::getId));

        Medication selected = null;
        if (!meds.isEmpty()) {
            if (id != null) {
                selected = meds.stream()
                        .filter(m -> Objects.equals(m.getId(), id))
                        .findFirst()
                        .orElse(meds.get(0));
            } else {
                selected = meds.get(0);
            }
        }

        // które leki user może edytować (do klodki)
        Set<Long> editableMedicationIds = new HashSet<>();
        for (Medication m : meds) {
            if (canEdit(loggedUser, m)) editableMedicationIds.add(m.getId());
        }

        boolean canEditSelected = selected != null && canEdit(loggedUser, selected);

        model.addAttribute("welcomeName", loggedUser.getFirstName());
        model.addAttribute("userFullName", targetUser.getFullName());
        model.addAttribute("medications", meds);

        model.addAttribute("selected", selected);
        model.addAttribute("canEditSelected", canEditSelected);

        model.addAttribute("canAddSelfMedication", viewingOwn);
        model.addAttribute("editableMedicationIds", editableMedicationIds);

        // żeby linki i redirecty działały w widoku pacjenta
        model.addAttribute("targetPatientId", patientId);

        return "medications";
    }


    @PostMapping("/medications/new")
    public String addNewMedication(
            Authentication authentication,
            @RequestParam String name,
            @RequestParam String dose,
            @RequestParam(required = false) String intakeTime,
            @RequestParam(required = false) Long returnToId
    ) {
        var loggedUser = userRepository.findByEmail(authentication.getName()).orElseThrow();

        String safeName = (name == null || name.isBlank()) ? "Nowy lek" : name.trim();
        String safeDose = (dose == null || dose.isBlank()) ? "1 tabletka" : dose.trim();
        LocalTime time = (intakeTime == null || intakeTime.isBlank()) ? null : LocalTime.parse(intakeTime);

        Medication m = Medication.builder()
                .name(safeName)
                .dose(safeDose)
                .intakeTime(time)
                .status(SCHEDULED)
                .createdByDoctor(false)
                .patient(loggedUser)
                .doctor(null)
                .build();

        Medication saved = medicationService.addMedicationSelf(loggedUser, m);

        Long backTo = (returnToId != null) ? returnToId : saved.getId();
        return "redirect:/medications?id=" + backTo;
    }

    @PostMapping("/medications/save")
    public String saveMedication(
            Authentication authentication,
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String dose,
            @RequestParam(required = false) String intakeTime,
            @RequestParam(required = false) Long patientId
    ) {
        var loggedUser = userRepository.findByEmail(authentication.getName()).orElseThrow();

        Medication update = new Medication();
        update.setName(name);
        update.setDose(dose);
        update.setIntakeTime(intakeTime == null || intakeTime.isBlank() ? null : LocalTime.parse(intakeTime));

        Medication current = medicationService.getMedicationById(id);

        if (patientId != null) {
            if (current.getPatient() == null || !Objects.equals(current.getPatient().getId(), patientId)) {
                return "redirect:/medications?patientId=" + patientId;
            }
        }

        if (!current.isCreatedByDoctor()) {
            medicationService.updateMedicationByPatient(loggedUser, id, update);
        } else {
            medicationService.updateMedicationByDoctor(loggedUser, id, update);
        }

        if (patientId != null) {
            return "redirect:/medications?patientId=" + patientId + "&id=" + id;
        }
        return "redirect:/medications?id=" + id;
    }

    private boolean canEdit(com.example.MedicalApplication.model.User user, Medication med) {
        if (!med.isCreatedByDoctor()) {
            return med.getPatient() != null && Objects.equals(med.getPatient().getId(), user.getId());
        }
        return med.getDoctor() != null && Objects.equals(med.getDoctor().getId(), user.getId());
    }

    private String roleName(Object role) {
        if (role == null) return "";
        if (role instanceof Enum<?> e) return e.name();
        return String.valueOf(role);
    }

    private boolean isDoctor(Object role) {
        String r = roleName(role);
        return "DOCTOR".equals(r) || "ROLE_DOCTOR".equals(r);
    }
}

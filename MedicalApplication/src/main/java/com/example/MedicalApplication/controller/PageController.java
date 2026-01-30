package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.model.Medication;
import com.example.MedicalApplication.model.MedicationStatus;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.repository.MedicationRepository;
import com.example.MedicalApplication.repository.UserRepository;
import com.example.MedicalApplication.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {
    private final UserRepository userRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationService medicationService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String homeAfterLogin(Model model, Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        model.addAttribute("welcomeName", user.getFirstName());

        return "home";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        model.addAttribute("welcomeName", user.getFirstName());
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("birthDate", user.getBirthDate());
        model.addAttribute("nfz", user.getNfzBranch());
        model.addAttribute("description", user.getDescription());

        return "profile";
    }
    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam(required = false) String birthDate,
            @RequestParam(required = false) String nfz,
            @RequestParam(required = false) String description,
            Authentication authentication,
            RedirectAttributes ra
    ) {
        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        user.setBirthDate(birthDate);
        user.setNfzBranch(nfz);
        user.setDescription(description);

        userRepository.save(user);

        ra.addFlashAttribute("saved", true);
        return "redirect:/profile";
    }

    @GetMapping("/status")
    public String showStatusPage(Model model, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Medication> meds = medicationService.getMedicationsForUser(user);

        long total = meds.size();
        long taken = meds.stream().filter(m -> m.getStatus() == MedicationStatus.TAKEN).count();
        long scheduled = total - taken;
        int pct = (total == 0) ? 0 : (int) Math.round((taken * 100.0) / total);

        model.addAttribute("welcomeName", user.getFullName());
        model.addAttribute("medications", meds);

        model.addAttribute("total", total);
        model.addAttribute("takenCount", taken);
        model.addAttribute("scheduledCount", scheduled);
        model.addAttribute("pct", pct);

        model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        return "status";
    }

    @PostMapping("/status/{id}/take")
    public String markTaken(@PathVariable Long id, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        medicationService.markAsTaken(user, id);
        return "redirect:/status";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(Model model, Authentication authentication,
                                     @RequestParam(required = false) Boolean changed) {
        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        model.addAttribute("welcomeName", user.getFirstName());
        model.addAttribute("changed", changed != null && changed);

        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(@RequestParam String oldPassword,
                                       @RequestParam String newPassword,
                                       @RequestParam String newPassword2,
                                       Authentication authentication,
                                       Model model) {

        var user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        // potrzebne, bo przy błędzie wracamy na widok, a nie redirect
        model.addAttribute("welcomeName", user.getFirstName());

        // 1) nowe hasła muszą być takie same
        if (newPassword == null || !newPassword.equals(newPassword2)) {
            model.addAttribute("error", "Nowe hasła nie są takie same.");
            return "change-password";
        }

        // 2) stare hasło musi pasować do tego z bazy
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("error", "Stare hasło jest nieprawidłowe.");
            return "change-password";
        }

        // 3) ustaw nowe hasło (zakodowane)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "redirect:/change-password?changed=true";
    }
}

package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.dto.RegisterRequest;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.model.UserRole;
import com.example.MedicalApplication.service.PwzVerifierService;
import com.example.MedicalApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RegisterPageController {

    private final UserService userService;
    private final PwzVerifierService pwzVerifier;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("roles", UserRole.values());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("registerRequest") RegisterRequest req, RedirectAttributes ra) {

        if (req.getRole() == UserRole.DOCTOR) {
            if (req.getPwz() == null || req.getPwz().isBlank()) {
                ra.addFlashAttribute("error", "PWZ jest wymagane dla lekarza");
                return "redirect:/register";
            }
            if (!pwzVerifier.isValid(req.getPwz())) {
                ra.addFlashAttribute("error", "Nieprawidłowy numer PWZ");
                return "redirect:/register";
            }
        }

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(req.getPassword())
                .role(req.getRole() != null ? req.getRole() : UserRole.PATIENT)
                .loggedIn(false)
                .build();

        userService.register(user);

        ra.addFlashAttribute("msg", "Konto utworzone. Zaloguj się");
        return "redirect:/login";
    }
}

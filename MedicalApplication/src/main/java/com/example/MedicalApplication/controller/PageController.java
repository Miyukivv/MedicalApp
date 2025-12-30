package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PageController {
    private final UserRepository userRepository;

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
    public String homeAfterLogin() {
        return "index";
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
}

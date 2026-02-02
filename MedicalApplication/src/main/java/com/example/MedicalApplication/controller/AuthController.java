package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.dto.RegisterRequest;
import com.example.MedicalApplication.model.User;
import com.example.MedicalApplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getEmail() == null || req.getPassword() == null
                || req.getFirstName() == null || req.getLastName() == null
                || req.getRole() == null) {
            return ResponseEntity.badRequest()
                    .body("firstName, lastName, email, password and role must be provided.");
        }

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(req.getPassword())
                .role(req.getRole())
                .loggedIn(false)
                .build();

        userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }

    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        User user = userService.login(email, password)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        user.setLoggedIn(true);
        userService.save(user);

        return user;
    }
}

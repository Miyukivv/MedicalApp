package com.example.MedicalApplication.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Service
public class PwzVerifierService {

    private final Set<String> allowed = new HashSet<>();

    public PwzVerifierService() throws IOException {
        ClassPathResource res = new ClassPathResource("/data/pwz.csv");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {

            br.lines()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .skip(1)                 // <- pomija nagłówek
                    .forEach(allowed::add);
        }
    }

    public boolean isValid(String pwz) {
        if (pwz == null) return false;
        return allowed.contains(pwz.trim());
    }
}

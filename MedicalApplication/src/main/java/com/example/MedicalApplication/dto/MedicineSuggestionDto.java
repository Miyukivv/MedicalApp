package com.example.MedicalApplication.dto;

public class MedicineSuggestionDto {
    public String name;
    public String uses;

    public MedicineSuggestionDto() {
    }

    public MedicineSuggestionDto(String name, String uses) {
        this.name = name;
        this.uses = uses;
    }
}
package com.example.MedicalApplication.dto;
import com.example.MedicalApplication.model.UserRole;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class RegisterRequest {
    private String name;
    private String emai;
    private String password;
    private UserRole role;
}

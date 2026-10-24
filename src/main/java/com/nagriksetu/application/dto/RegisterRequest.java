package com.nagriksetu.application.dto;

import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank String fullName, @Email @NotBlank String email, @Size(min = 6, message = "Password must be at least 6 characters") String password, @NotBlank String role, String department) {}

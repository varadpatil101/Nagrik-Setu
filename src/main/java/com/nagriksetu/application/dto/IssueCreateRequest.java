package com.nagriksetu.application.dto;

import jakarta.validation.constraints.NotBlank;

public record IssueCreateRequest(@NotBlank String title, @NotBlank String description, @NotBlank String category, @NotBlank String department, String location, Double latitude, Double longitude, String imageUrl) {}

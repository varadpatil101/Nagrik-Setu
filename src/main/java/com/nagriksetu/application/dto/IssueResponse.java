package com.nagriksetu.application.dto;

import java.time.LocalDateTime;
public record IssueResponse(Long id, String title, String description, String category, String location, Double latitude, Double longitude, String imageUrl, String resolutionEvidenceUrl, String department, String priority, String status, LocalDateTime createdAt) {}

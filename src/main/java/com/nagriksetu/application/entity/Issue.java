package com.nagriksetu.application.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issues")
@Getter @Setter @NoArgsConstructor
public class Issue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false, length = 1500) private String description;
    @Column(nullable = false) private String category;
    private String location;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String resolutionEvidenceUrl;
    private String department;
    private String priority = "3";
    private String status = "Reported";
    private Long reporterId;
    private LocalDateTime createdAt = LocalDateTime.now();
}

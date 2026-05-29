package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private Long id;
    private String type;
    private String priority;
    private String message;

    private Double predictedImpact;

    private Double interventionEffectiveness;

    private Double operationalConfidence;
    private String status;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNotes;

    private Long districtId;

    private String districtName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

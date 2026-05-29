package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private Long id;
    private String title;
    private String type;
    private String description;
    private String severity;
    private Double latitude;
    private Double longitude;

    private Long districtId;

    private String districtName;

    private String status;
    private String assignedTo;
    private Boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private Boolean reviewed;
    private LocalDateTime reviewedAt;
    private String reviewNotes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

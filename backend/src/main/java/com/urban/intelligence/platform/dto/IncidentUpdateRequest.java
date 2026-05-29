package com.urban.intelligence.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentUpdateRequest {
    private String type;
    private String description;
    private String severity;
    private Double latitude;
    private Double longitude;
    private String status;
    private String assignedTo;
    private Boolean acknowledged;
    private Boolean reviewed;
    private String reviewNotes;
}

package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private Long id;
    private String type;
    private String description;
    private String severity;
    private Double latitude;
    private Double longitude;

    @JsonProperty("district_id")
    private Long districtId;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

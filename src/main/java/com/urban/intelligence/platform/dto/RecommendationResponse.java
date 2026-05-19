package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("district_id")
    private Long districtId;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

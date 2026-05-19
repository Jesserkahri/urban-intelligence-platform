package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsTrendResponse {
    private String category;

    @JsonProperty("trend_direction")
    private String trendDirection;

    @JsonProperty("change_percentage")
    private Double changePercentage;

    @JsonProperty("recent_average")
    private Double recentAverage;

    @JsonProperty("previous_average")
    private Double previousAverage;
}

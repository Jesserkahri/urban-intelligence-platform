package com.urban.intelligence.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsAggregateResponse {
    @JsonProperty("total_events")
    private Long totalEvents;

    @JsonProperty("average_score")
    private Double averageScore;

    @JsonProperty("highest_score")
    private Double highestScore;

    @JsonProperty("lowest_score")
    private Double lowestScore;

    private String category;

    @JsonProperty("time_period")
    private String timePeriod;
}

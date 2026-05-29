package com.urban.intelligence.platform.dto.predictive;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvancedAnalyticsResponse {
    private Integer analysisWindowDays;
    private Map<String, Double> dayOfWeekSeasonality;
    private List<String> recurringAnomalyCategories;
    private Double anomalyPressureScore;
    private String seasonalPatternSummary;
    private LocalDateTime generatedAt;
}

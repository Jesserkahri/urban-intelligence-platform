package com.urban.intelligence.platform.dto.predictive;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictiveOverviewResponse {
    private List<IncidentForecastResponse> incidentForecasts;
    private List<DistrictRiskForecastResponse> riskForecasts;
    private List<PredictiveAlertResponse> earlyWarnings;
    private AdvancedAnalyticsResponse advancedAnalytics;
    private LocalDateTime generatedAt;
}

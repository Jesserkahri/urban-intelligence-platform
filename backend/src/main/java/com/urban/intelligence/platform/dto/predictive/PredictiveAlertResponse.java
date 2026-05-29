package com.urban.intelligence.platform.dto.predictive;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictiveAlertResponse {
    private Long id;
    private Long districtId;
    private String districtName;
    private String alertType;
    private String severity;
    private String title;
    private String message;
    private Double probability;
    private Double confidence;
    private Integer forecastWindowDays;
    private Double predictedValue;
    private Double baselineValue;
    private LocalDateTime createdAt;
}

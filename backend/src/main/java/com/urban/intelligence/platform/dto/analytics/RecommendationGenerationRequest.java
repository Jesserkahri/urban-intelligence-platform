package com.urban.intelligence.platform.dto.analytics;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationGenerationRequest {
    private Long districtId;
    private boolean includeTrafficAnalysis;
    private boolean includeInfrastructureAnalysis;
    private boolean includeSustainabilityAnalysis;
}

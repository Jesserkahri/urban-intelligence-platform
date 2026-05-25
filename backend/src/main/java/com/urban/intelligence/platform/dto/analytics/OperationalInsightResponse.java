package com.urban.intelligence.platform.dto.analytics;

import com.urban.intelligence.platform.domain.entity.Recommendation;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationalInsightResponse {
    private Long districtId;
    private String districtName;
    private Integer generatedRecommendationCount;
    private Integer criticalRecommendations;
    private Integer highPriorityRecommendations;
    private List<Recommendation> recommendations;
    private LocalDateTime generatedAt;
}

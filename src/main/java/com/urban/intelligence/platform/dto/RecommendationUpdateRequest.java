package com.urban.intelligence.platform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationUpdateRequest {
    private String type;
    private String priority;
    private String message;
}

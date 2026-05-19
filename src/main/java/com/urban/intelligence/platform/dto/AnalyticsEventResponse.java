package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEventResponse {
    private Long id;
    private String category;
    private Double score;
    private String source;
    private LocalDateTime timestamp;
    private String metadata;
}

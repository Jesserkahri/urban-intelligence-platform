package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalNotificationResponse {
    private Long id;
    private String type;
    private String severity;
    private String title;
    private String message;
    private Long incidentId;
    private Long districtId;
    private Boolean acknowledged;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

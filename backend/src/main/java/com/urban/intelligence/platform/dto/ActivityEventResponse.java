package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityEventResponse {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String actor;
    private String details;
    private LocalDateTime createdAt;
}

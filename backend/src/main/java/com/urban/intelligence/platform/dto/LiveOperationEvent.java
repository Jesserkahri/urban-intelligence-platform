package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveOperationEvent {
    private String id;
    private String type;
    private String channel;
    private String severity;
    private String title;
    private String message;
    private Long incidentId;
    private Long districtId;
    private LocalDateTime occurredAt;
    private Map<String, Object> payload;
}

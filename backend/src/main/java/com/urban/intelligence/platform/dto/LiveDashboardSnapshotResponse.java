package com.urban.intelligence.platform.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveDashboardSnapshotResponse {
    private Long activeIncidents;
    private Long criticalIncidents24h;
    private Long unresolvedIncidents;
    private Long unreadNotifications;
    private Long alerts24h;
    private Map<String, Long> statusCounters;
    private Map<String, Long> severityCounters;
    private LocalDateTime generatedAt;
}

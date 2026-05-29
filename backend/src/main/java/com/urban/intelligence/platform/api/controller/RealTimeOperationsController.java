package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.dto.LiveDashboardSnapshotResponse;
import com.urban.intelligence.platform.dto.OperationalNotificationResponse;
import com.urban.intelligence.platform.realtime.RealTimeOperationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
@Slf4j
public class RealTimeOperationsController {

    private final RealTimeOperationsService realTimeOperationsService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(defaultValue = "all") String channel) {
        log.info("SSE subscribe channel={}", channel);
        return realTimeOperationsService.subscribe(channel);
    }

    @GetMapping("/dashboard/live")
    public ResponseEntity<ApiResponse<LiveDashboardSnapshotResponse>> getLiveDashboardSnapshot() {
        return ResponseEntity.ok(ApiResponse.success(realTimeOperationsService.getDashboardSnapshot()));
    }

    @PostMapping("/dashboard/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<Void>> publishDashboardSnapshot() {
        realTimeOperationsService.publishDashboardSnapshot();
        return ResponseEntity.accepted().body(ApiResponse.success(null, "Dashboard snapshot published"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Page<OperationalNotificationResponse>>> getNotifications(
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) String severity,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                realTimeOperationsService.getNotifications(pageable, unreadOnly, severity)));
    }

    @PostMapping("/notifications/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<OperationalNotificationResponse>> acknowledgeNotification(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                realTimeOperationsService.acknowledgeNotification(id),
                "Notification acknowledged"));
    }
}

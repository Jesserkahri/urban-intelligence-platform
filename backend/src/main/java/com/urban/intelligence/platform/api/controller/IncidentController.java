package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.dto.IncidentCreateRequest;
import com.urban.intelligence.platform.dto.IncidentResponse;
import com.urban.intelligence.platform.dto.IncidentUpdateRequest;
import com.urban.intelligence.platform.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident(@Valid @RequestBody IncidentCreateRequest request) {
        log.info("CREATE incident");
        IncidentResponse response = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Incident created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncident(@PathVariable Long id) {
        log.debug("READ incident: {}", id);
        IncidentResponse response = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getAllIncidents(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("READ all incidents");
        Page<IncidentResponse> response = incidentService.getAllIncidents(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/district/{districtId}")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getIncidentsByDistrict(
            @PathVariable Long districtId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("READ incidents for district: {}", districtId);
        Page<IncidentResponse> response = incidentService.getIncidentsByDistrict(districtId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getIncidentsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("READ incidents by status: {}", status);
        Page<IncidentResponse> response = incidentService.getIncidentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/district/{districtId}/active")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getActiveIncidents(@PathVariable Long districtId) {
        log.debug("READ active incidents for district: {}", districtId);
        List<IncidentResponse> response = incidentService.getActiveIncidentsByDistrict(districtId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getRecentIncidents() {
        log.debug("READ recent incidents");
        List<IncidentResponse> response = incidentService.getRecentIncidents();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request) {
        log.info("UPDATE incident: {}", id);
        IncidentResponse response = incidentService.updateIncident(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Incident updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteIncident(@PathVariable Long id) {
        log.info("DELETE incident: {}", id);
        incidentService.deleteIncident(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Incident deleted successfully"));
    }
}
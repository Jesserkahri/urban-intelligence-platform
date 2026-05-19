package com.urban.intelligence.platform.api.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IncidentController - REST endpoints for incident management
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * Create a new incident
     * POST /api/incidents
     */
    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@Valid @RequestBody IncidentCreateRequest request) {
        log.info("POST /api/incidents - Creating new incident");
        IncidentResponse response = incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get incident by ID
     * GET /api/incidents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable Long id) {
        log.info("GET /api/incidents/{} - Fetching incident", id);
        IncidentResponse response = incidentService.getIncidentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all incidents with pagination
     * GET /api/incidents?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> getAllIncidents(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/incidents - Fetching all incidents");
        Page<IncidentResponse> response = incidentService.getAllIncidents(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get incidents by district
     * GET /api/incidents/district/{districtId}
     */
    @GetMapping("/district/{districtId}")
    public ResponseEntity<Page<IncidentResponse>> getIncidentsByDistrict(
            @PathVariable Long districtId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/incidents/district/{} - Fetching incidents by district", districtId);
        Page<IncidentResponse> response = incidentService.getIncidentsByDistrict(districtId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get incidents by status
     * GET /api/incidents/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<IncidentResponse>> getIncidentsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/incidents/status/{} - Fetching incidents by status", status);
        Page<IncidentResponse> response = incidentService.getIncidentsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active incidents for a district
     * GET /api/incidents/district/{districtId}/active
     */
    @GetMapping("/district/{districtId}/active")
    public ResponseEntity<List<IncidentResponse>> getActiveIncidents(@PathVariable Long districtId) {
        log.info("GET /api/incidents/district/{}/active - Fetching active incidents", districtId);
        List<IncidentResponse> response = incidentService.getActiveIncidentsByDistrict(districtId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent incidents (last 7 days)
     * GET /api/incidents/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<IncidentResponse>> getRecentIncidents() {
        log.info("GET /api/incidents/recent - Fetching recent incidents");
        List<IncidentResponse> response = incidentService.getRecentIncidents();
        return ResponseEntity.ok(response);
    }

    /**
     * Update incident
     * PUT /api/incidents/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody IncidentUpdateRequest request) {
        log.info("PUT /api/incidents/{} - Updating incident", id);
        IncidentResponse response = incidentService.updateIncident(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete incident
     * DELETE /api/incidents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        log.info("DELETE /api/incidents/{} - Deleting incident", id);
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
}

package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.dto.DistrictCreateRequest;
import com.urban.intelligence.platform.dto.DistrictResponse;
import com.urban.intelligence.platform.dto.DistrictUpdateRequest;
import com.urban.intelligence.platform.dto.DistrictMetricsResponse;
import com.urban.intelligence.platform.service.DistrictService;
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
 * DistrictController - REST endpoints for district management
 */
@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
@Slf4j
public class DistrictController {

    private final DistrictService districtService;

    /**
     * Create a new district
     * POST /api/districts
     */
    @PostMapping
    public ResponseEntity<DistrictResponse> createDistrict(@Valid @RequestBody DistrictCreateRequest request) {
        log.info("POST /api/districts - Creating new district: {}", request.getName());
        DistrictResponse response = districtService.createDistrict(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get district by ID
     * GET /api/districts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DistrictResponse> getDistrict(@PathVariable Long id) {
        log.info("GET /api/districts/{} - Fetching district", id);
        DistrictResponse response = districtService.getDistrictById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all districts with pagination
     * GET /api/districts?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<DistrictResponse>> getAllDistricts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/districts - Fetching all districts");
        Page<DistrictResponse> response = districtService.getAllDistricts(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get district metrics with health score
     * GET /api/districts/{id}/metrics
     */
    @GetMapping("/{id}/metrics")
    public ResponseEntity<DistrictMetricsResponse> getDistrictMetrics(@PathVariable Long id) {
        log.info("GET /api/districts/{}/metrics - Fetching district metrics", id);
        DistrictMetricsResponse response = districtService.getDistrictMetrics(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get districts ranked by highest risk
     * GET /api/districts/risk-analysis/highest
     */
    @GetMapping("/risk-analysis/highest")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByHighestRisk() {
        log.info("GET /api/districts/risk-analysis/highest - Fetching districts by highest risk");
        List<DistrictResponse> response = districtService.getDistrictsByHighestRisk();
        return ResponseEntity.ok(response);
    }

    /**
     * Get districts below sustainability threshold
     * GET /api/districts/sustainability/below?threshold=70
     */
    @GetMapping("/sustainability/below")
    public ResponseEntity<List<DistrictResponse>> getDistrictsBelowSustainabilityThreshold(
            @RequestParam Double threshold) {
        log.info("GET /api/districts/sustainability/below?threshold={}", threshold);
        List<DistrictResponse> response = districtService.getDistrictsBelowSustainabilityThreshold(threshold);
        return ResponseEntity.ok(response);
    }

    /**
     * Update district
     * PUT /api/districts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DistrictResponse> updateDistrict(
            @PathVariable Long id,
            @Valid @RequestBody DistrictUpdateRequest request) {
        log.info("PUT /api/districts/{} - Updating district", id);
        DistrictResponse response = districtService.updateDistrict(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete district
     * DELETE /api/districts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDistrict(@PathVariable Long id) {
        log.info("DELETE /api/districts/{} - Deleting district", id);
        districtService.deleteDistrict(id);
        return ResponseEntity.noContent().build();
    }
}

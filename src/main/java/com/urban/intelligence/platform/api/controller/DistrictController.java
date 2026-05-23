package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.dto.ApiResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DistrictResponse>> createDistrict(@Valid @RequestBody DistrictCreateRequest request) {
        log.info("CREATE district: {}", request.getName());
        DistrictResponse response = districtService.createDistrict(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "District created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DistrictResponse>> getDistrict(@PathVariable Long id) {
        log.debug("READ district: {}", id);
        DistrictResponse response = districtService.getDistrictById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DistrictResponse>>> getAllDistricts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("READ all districts");
        Page<DistrictResponse> response = districtService.getAllDistricts(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<ApiResponse<DistrictMetricsResponse>> getDistrictMetrics(@PathVariable Long id) {
        log.debug("READ metrics for district: {}", id);
        DistrictMetricsResponse response = districtService.getDistrictMetrics(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/risk-analysis/highest")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getDistrictsByHighestRisk() {
        log.debug("READ districts by highest risk");
        List<DistrictResponse> response = districtService.getDistrictsByHighestRisk();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sustainability/below")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getDistrictsBelowSustainabilityThreshold(
            @RequestParam Double threshold) {
        log.debug("READ districts below sustainability: {}", threshold);
        List<DistrictResponse> response = districtService.getDistrictsBelowSustainabilityThreshold(threshold);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DistrictResponse>> updateDistrict(
            @PathVariable Long id,
            @Valid @RequestBody DistrictUpdateRequest request) {
        log.info("UPDATE district: {}", id);
        DistrictResponse response = districtService.updateDistrict(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "District updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDistrict(@PathVariable Long id) {
        log.info("DELETE district: {}", id);
        districtService.deleteDistrict(id);
        return ResponseEntity.ok(ApiResponse.success(null, "District deleted successfully"));
    }
}
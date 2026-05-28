package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.dto.RecommendationCreateRequest;
import com.urban.intelligence.platform.dto.RecommendationResponse;
import com.urban.intelligence.platform.dto.RecommendationUpdateRequest;
import com.urban.intelligence.platform.service.RecommendationService;
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
 * RecommendationController - REST endpoints for recommendation management
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Create a new recommendation
     * POST /api/recommendations
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<RecommendationResponse>> createRecommendation(
            @Valid @RequestBody RecommendationCreateRequest request) {
        log.info("POST /api/recommendations - Creating new recommendation");
        RecommendationResponse response = recommendationService.createRecommendation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Recommendation created successfully"));
    }

    /**
     * Get recommendation by ID
     * GET /api/recommendations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendation(@PathVariable Long id) {
        log.info("GET /api/recommendations/{} - Fetching recommendation", id);
        RecommendationResponse response = recommendationService.getRecommendationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all recommendations with pagination
     * GET /api/recommendations?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RecommendationResponse>>> getAllRecommendations(
            @PageableDefault(size = 20, sort = "priority", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/recommendations - Fetching all recommendations");
        Page<RecommendationResponse> response = recommendationService.getAllRecommendations(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get recommendations by district
     * GET /api/recommendations/district/{districtId}
     */
    @GetMapping("/district/{districtId}")
    public ResponseEntity<ApiResponse<Page<RecommendationResponse>>> getRecommendationsByDistrict(
            @PathVariable Long districtId,
            @PageableDefault(size = 20, sort = "priority", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/recommendations/district/{} - Fetching recommendations by district", districtId);
        Page<RecommendationResponse> response = recommendationService.getRecommendationsByDistrict(districtId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get recommendations by priority
     * GET /api/recommendations/priority/{priority}
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<Page<RecommendationResponse>>> getRecommendationsByPriority(
            @PathVariable String priority,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/recommendations/priority/{} - Fetching recommendations by priority", priority);
        Page<RecommendationResponse> response = recommendationService.getRecommendationsByPriority(priority, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get urgent recommendations for a district
     * GET /api/recommendations/district/{districtId}/urgent
     */
    @GetMapping("/district/{districtId}/urgent")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getUrgentRecommendations(@PathVariable Long districtId) {
        log.info("GET /api/recommendations/district/{}/urgent - Fetching urgent recommendations", districtId);
        List<RecommendationResponse> response = recommendationService.getUrgentRecommendations(districtId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update recommendation
     * PUT /api/recommendations/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<ApiResponse<RecommendationResponse>> updateRecommendation(
            @PathVariable Long id,
            @Valid @RequestBody RecommendationUpdateRequest request) {
        log.info("PUT /api/recommendations/{} - Updating recommendation", id);
        RecommendationResponse response = recommendationService.updateRecommendation(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendation updated successfully"));
    }

    /**
     * Delete recommendation
     * DELETE /api/recommendations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        log.info("DELETE /api/recommendations/{} - Deleting recommendation", id);
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.noContent().build();
    }
}

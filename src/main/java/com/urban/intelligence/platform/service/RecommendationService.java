package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.api.exception.ResourceNotFoundException;
import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Recommendation;
import com.urban.intelligence.platform.domain.repository.DistrictRepository;
import com.urban.intelligence.platform.domain.repository.RecommendationRepository;
import com.urban.intelligence.platform.dto.RecommendationCreateRequest;
import com.urban.intelligence.platform.dto.RecommendationResponse;
import com.urban.intelligence.platform.dto.RecommendationUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RecommendationService - Business logic for recommendation management
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final DistrictRepository districtRepository;

    /**
     * Create a new recommendation
     */
    public RecommendationResponse createRecommendation(RecommendationCreateRequest request) {
        log.info("Creating new recommendation of type: {}", request.getType());
        
        District district = districtRepository.findById(request.getDistrictId())
            .orElseThrow(() -> new ResourceNotFoundException("District not found with ID: " + request.getDistrictId()));

        Recommendation recommendation = Recommendation.builder()
            .type(request.getType())
            .priority(Recommendation.Priority.valueOf(request.getPriority()))
            .message(request.getMessage())
            .district(district)
            .build();

        Recommendation savedRecommendation = recommendationRepository.save(recommendation);
        log.info("Recommendation created successfully with ID: {}", savedRecommendation.getId());
        
        return mapToResponse(savedRecommendation);
    }

    /**
     * Get recommendation by ID
     */
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendationById(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));
        return mapToResponse(recommendation);
    }

    /**
     * Get all recommendations with pagination
     */
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getAllRecommendations(Pageable pageable) {
        log.debug("Fetching all recommendations with pagination");
        return recommendationRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get recommendations by district
     */
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByDistrict(Long districtId, Pageable pageable) {
        log.debug("Fetching recommendations for district: {}", districtId);
        
        if (!districtRepository.existsById(districtId)) {
            throw new ResourceNotFoundException("District not found with ID: " + districtId);
        }
        
        return recommendationRepository.findByDistrict_Id(districtId, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get recommendations by priority
     */
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByPriority(String priority, Pageable pageable) {
        log.debug("Fetching recommendations by priority: {}", priority);
        Recommendation.Priority priorityLevel = Recommendation.Priority.valueOf(priority.toUpperCase());
        return recommendationRepository.findByPriority(priorityLevel, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Update recommendation
     */
    public RecommendationResponse updateRecommendation(Long id, RecommendationUpdateRequest request) {
        log.info("Updating recommendation with ID: {}", id);
        
        Recommendation recommendation = recommendationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));

        if (request.getType() != null) recommendation.setType(request.getType());
        if (request.getPriority() != null) recommendation.setPriority(Recommendation.Priority.valueOf(request.getPriority()));
        if (request.getMessage() != null) recommendation.setMessage(request.getMessage());

        Recommendation updatedRecommendation = recommendationRepository.save(recommendation);
        log.info("Recommendation updated successfully");
        
        return mapToResponse(updatedRecommendation);
    }

    /**
     * Delete recommendation
     */
    public void deleteRecommendation(Long id) {
        log.info("Deleting recommendation with ID: {}", id);
        
        if (!recommendationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recommendation not found with ID: " + id);
        }
        
        recommendationRepository.deleteById(id);
        log.info("Recommendation deleted successfully");
    }

    /**
     * Get urgent recommendations for a district
     */
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getUrgentRecommendations(Long districtId) {
        log.debug("Fetching urgent recommendations for district: {}", districtId);
        return recommendationRepository.findUrgentRecommendationsByDistrict(districtId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Helper method to convert Recommendation to RecommendationResponse DTO
     */
    private RecommendationResponse mapToResponse(Recommendation recommendation) {
        return RecommendationResponse.builder()
            .id(recommendation.getId())
            .type(recommendation.getType())
            .priority(recommendation.getPriority().toString())
            .message(recommendation.getMessage())
            .districtId(recommendation.getDistrict().getId())
            .districtName(recommendation.getDistrict().getName())
            .createdAt(recommendation.getCreatedAt())
            .updatedAt(recommendation.getUpdatedAt())
            .build();
    }
}

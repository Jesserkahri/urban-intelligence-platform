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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final DistrictRepository districtRepository;

    public RecommendationResponse createRecommendation(RecommendationCreateRequest request) {
        District district = districtRepository.findById(request.getDistrictId())
            .orElseThrow(() -> new ResourceNotFoundException("District not found with ID: " + request.getDistrictId()));

        Recommendation recommendation = Recommendation.builder()
            .type(request.getType())
            .priority(Recommendation.Priority.valueOf(request.getPriority().toUpperCase()))
            .message(request.getMessage())
            .district(district)
            .build();

        return mapToResponse(recommendationRepository.save(recommendation));
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendationById(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));
        return mapToResponse(recommendation);
    }

    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getAllRecommendations(Pageable pageable) {
        return recommendationRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByDistrict(Long districtId, Pageable pageable) {
        if (!districtRepository.existsById(districtId)) {
            throw new ResourceNotFoundException("District not found with ID: " + districtId);
        }
        return recommendationRepository.findByDistrict_Id(districtId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RecommendationResponse> getRecommendationsByPriority(String priority, Pageable pageable) {
        Recommendation.Priority priorityLevel = Recommendation.Priority.valueOf(priority.toUpperCase());
        return recommendationRepository.findByPriority(priorityLevel, pageable).map(this::mapToResponse);
    }

    public RecommendationResponse updateRecommendation(Long id, RecommendationUpdateRequest request) {
        Recommendation recommendation = recommendationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with ID: " + id));

        if (request.getType() != null) recommendation.setType(request.getType());
        if (request.getPriority() != null) recommendation.setPriority(Recommendation.Priority.valueOf(request.getPriority().toUpperCase()));
        if (request.getMessage() != null) recommendation.setMessage(request.getMessage());

        return mapToResponse(recommendationRepository.save(recommendation));
    }

    public void deleteRecommendation(Long id) {
        if (!recommendationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recommendation not found with ID: " + id);
        }
        recommendationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getUrgentRecommendations(Long districtId) {
        return recommendationRepository.findUrgentRecommendationsByDistrict(districtId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private RecommendationResponse mapToResponse(Recommendation recommendation) {
        return RecommendationResponse.builder()
            .id(recommendation.getId())
            .type(recommendation.getType())
            .priority(recommendation.getPriority().toString())
            .message(recommendation.getMessage())
            .predictedImpact(recommendation.getPredictedImpact())
            .interventionEffectiveness(recommendation.getInterventionEffectiveness())
            .operationalConfidence(recommendation.getOperationalConfidence())
            .districtId(recommendation.getDistrict().getId())
            .districtName(recommendation.getDistrict().getName())
            .createdAt(recommendation.getCreatedAt())
            .updatedAt(recommendation.getUpdatedAt())
            .build();
    }
}

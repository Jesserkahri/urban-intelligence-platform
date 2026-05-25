package com.urban.intelligence.platform.service;

import com.urban.intelligence.platform.api.exception.ResourceNotFoundException;
import com.urban.intelligence.platform.domain.entity.AnalyticsEvent;
import com.urban.intelligence.platform.domain.repository.AnalyticsEventRepository;
import com.urban.intelligence.platform.dto.AnalyticsEventCreateRequest;
import com.urban.intelligence.platform.dto.AnalyticsEventResponse;
import com.urban.intelligence.platform.dto.AnalyticsAggregateResponse;
import com.urban.intelligence.platform.dto.AnalyticsTrendResponse;
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
 * AnalyticsEventService - Business logic for analytics event management
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventService {

    private final AnalyticsEventRepository analyticsEventRepository;

    /**
     * Record a new analytics event
     */
    public AnalyticsEventResponse recordEvent(AnalyticsEventCreateRequest request) {
        log.info("Recording analytics event - Category: {}, Source: {}", request.getCategory(), request.getSource());
        
        AnalyticsEvent event = AnalyticsEvent.builder()
            .category(request.getCategory())
            .score(request.getScore())
            .source(request.getSource())
            .metadata(request.getMetadata())
            .build();

        AnalyticsEvent savedEvent = analyticsEventRepository.save(event);
        log.debug("Analytics event recorded with ID: {}", savedEvent.getId());
        
        return mapToResponse(savedEvent);
    }

    /**
     * Get analytics event by ID
     */
    @Transactional(readOnly = true)
    public AnalyticsEventResponse getEventById(Long id) {
        AnalyticsEvent event = analyticsEventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Analytics event not found with ID: " + id));
        return mapToResponse(event);
    }

    /**
     * Get all analytics events with pagination
     */
    @Transactional(readOnly = true)
    public Page<AnalyticsEventResponse> getAllEvents(Pageable pageable) {
        log.debug("Fetching all analytics events with pagination");
        return analyticsEventRepository.findAll(pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get events by category
     */
    @Transactional(readOnly = true)
    public Page<AnalyticsEventResponse> getEventsByCategory(String category, Pageable pageable) {
        log.debug("Fetching analytics events for category: {}", category);
        return analyticsEventRepository.findByCategory(category, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Get events by source
     */
    @Transactional(readOnly = true)
    public Page<AnalyticsEventResponse> getEventsBySource(String source, Pageable pageable) {
        log.debug("Fetching analytics events from source: {}", source);
        return analyticsEventRepository.findBySource(source, pageable)
            .map(this::mapToResponse);
    }

    /**
     * Delete analytics event
     */
    public void deleteEvent(Long id) {
        log.info("Deleting analytics event with ID: {}", id);
        
        if (!analyticsEventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Analytics event not found with ID: " + id);
        }
        
        analyticsEventRepository.deleteById(id);
        log.info("Analytics event deleted successfully");
    }

    /**
     * Get recent high-scoring events
     */
    @Transactional(readOnly = true)
    public List<AnalyticsEventResponse> getRecentHighScoringEvents() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return analyticsEventRepository.findRecentHighScoringEvents(twentyFourHoursAgo).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get aggregate statistics for a category
     */
    @Transactional(readOnly = true)
    public AnalyticsAggregateResponse getCategoryAggregates(String category) {
        log.debug("Computing aggregates for category: {}", category);
        
        Long totalEvents = analyticsEventRepository.countByCategory(category);
        Double averageScore = analyticsEventRepository.getAverageScoreByCategory(category);
        Double highestScore = analyticsEventRepository.getMaxScoreByCategory(category);
        Double lowestScore = analyticsEventRepository.getMinScoreByCategory(category);

        return AnalyticsAggregateResponse.builder()
            .totalEvents(totalEvents)
            .averageScore(averageScore)
            .highestScore(highestScore)
            .lowestScore(lowestScore)
            .category(category)
            .timePeriod("ALL_TIME")
            .build();
    }

    /**
     * Helper method to convert AnalyticsEvent to AnalyticsEventResponse DTO
     */
    private AnalyticsEventResponse mapToResponse(AnalyticsEvent event) {
        return AnalyticsEventResponse.builder()
            .id(event.getId())
            .category(event.getCategory())
            .score(event.getScore())
            .source(event.getSource())
            .timestamp(event.getTimestamp())
            .metadata(event.getMetadata())
            .build();
    }
}

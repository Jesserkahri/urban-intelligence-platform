package com.urban.intelligence.platform.api.controller;

import com.urban.intelligence.platform.analytics.DistrictRiskScoringService;
import com.urban.intelligence.platform.analytics.HotspotDetectionService;
import com.urban.intelligence.platform.analytics.OperationalInsightService;
import com.urban.intelligence.platform.analytics.TrendAggregationService;
import com.urban.intelligence.platform.domain.entity.Recommendation;
import com.urban.intelligence.platform.dto.AnalyticsEventCreateRequest;
import com.urban.intelligence.platform.dto.AnalyticsEventResponse;
import com.urban.intelligence.platform.dto.AnalyticsAggregateResponse;
import com.urban.intelligence.platform.dto.ApiResponse;
import com.urban.intelligence.platform.dto.analytics.CategoryTrendResponse;
import com.urban.intelligence.platform.dto.analytics.DailyTrendResponse;
import com.urban.intelligence.platform.dto.analytics.DistrictRiskAnalysisResponse;
import com.urban.intelligence.platform.dto.analytics.DistrictRiskRankingResponse;
import com.urban.intelligence.platform.dto.analytics.HotspotRankingResponse;
import com.urban.intelligence.platform.dto.analytics.HotspotResponse;
import com.urban.intelligence.platform.dto.analytics.DashboardInsightResponse;
import com.urban.intelligence.platform.dto.analytics.OperationalInsightResponse;
import com.urban.intelligence.platform.dto.analytics.WeeklyTrendResponse;
import com.urban.intelligence.platform.service.AnalyticsEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsEventService analyticsEventService;
    private final HotspotDetectionService hotspotDetectionService;
    private final TrendAggregationService trendAggregationService;
    private final DistrictRiskScoringService riskScoringService;
    private final OperationalInsightService insightService;

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<AnalyticsEventResponse> recordEvent(@Valid @RequestBody AnalyticsEventCreateRequest request) {
        log.info("CREATE analytics event");
        AnalyticsEventResponse response = analyticsEventService.recordEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<AnalyticsEventResponse> getEvent(@PathVariable Long id) {
        log.debug("READ analytics event: {}", id);
        AnalyticsEventResponse response = analyticsEventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    public ResponseEntity<Page<AnalyticsEventResponse>> getAllEvents(
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("READ all analytics events");
        Page<AnalyticsEventResponse> response = analyticsEventService.getAllEvents(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/category/{category}")
    public ResponseEntity<Page<AnalyticsEventResponse>> getEventsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("READ events by category: {}", category);
        Page<AnalyticsEventResponse> response = analyticsEventService.getEventsByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/source/{source}")
    public ResponseEntity<Page<AnalyticsEventResponse>> getEventsBySource(
            @PathVariable String source,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("READ events by source: {}", source);
        Page<AnalyticsEventResponse> response = analyticsEventService.getEventsBySource(source, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/recent/high-scoring")
    public ResponseEntity<List<AnalyticsEventResponse>> getRecentHighScoringEvents() {
        log.debug("READ recent high-scoring events");
        List<AnalyticsEventResponse> response = analyticsEventService.getRecentHighScoringEvents();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/aggregates/{category}")
    public ResponseEntity<AnalyticsAggregateResponse> getCategoryAggregates(@PathVariable String category) {
        log.debug("READ aggregates for category: {}", category);
        AnalyticsAggregateResponse response = analyticsEventService.getCategoryAggregates(category);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("DELETE analytics event: {}", id);
        analyticsEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hotspots/district/{districtId}")
    public ResponseEntity<HotspotResponse> detectDistrictHotspot(@PathVariable Long districtId) {
        return ResponseEntity.ok(hotspotDetectionService.detectDistrictHotspot(districtId));
    }

    @GetMapping("/hotspots")
    public ResponseEntity<List<HotspotResponse>> detectAllHotspots() {
        return ResponseEntity.ok(hotspotDetectionService.detectAllHotspots());
    }

    @GetMapping("/hotspots/top")
    public ResponseEntity<List<HotspotRankingResponse>> getTopCriticalHotspots(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(hotspotDetectionService.getTopCriticalHotspots(Math.min(limit, 50)));
    }

    @GetMapping("/trends/daily")
    public ResponseEntity<DailyTrendResponse> analyzeDailyTrends() {
        return ResponseEntity.ok(trendAggregationService.analyzeDailyTrends());
    }

    @GetMapping("/trends/weekly")
    public ResponseEntity<WeeklyTrendResponse> analyzeWeeklyTrends() {
        return ResponseEntity.ok(trendAggregationService.analyzeWeeklyTrends());
    }

    @GetMapping("/trends/categories")
    public ResponseEntity<CategoryTrendResponse> analyzeCategoryTrends() {
        return ResponseEntity.ok(trendAggregationService.analyzeCategoryTrends());
    }

    @GetMapping("/districts/risk-ranking")
    public ResponseEntity<List<DistrictRiskRankingResponse>> getRiskRanking() {
        return ResponseEntity.ok(riskScoringService.getRiskRanking());
    }

    @GetMapping("/districts/{districtId}/risk-analysis")
    public ResponseEntity<DistrictRiskAnalysisResponse> analyzeDistrictRisk(@PathVariable Long districtId) {
        return ResponseEntity.ok(riskScoringService.analyzeDistrictRisk(districtId));
    }

    @GetMapping("/districts/risk-level/{riskLevel}")
    public ResponseEntity<List<DistrictRiskRankingResponse>> getDistrictsByRiskLevel(@PathVariable String riskLevel) {
        return ResponseEntity.ok(riskScoringService.getDistrictsByRiskLevel(riskLevel.toUpperCase()));
    }

    @GetMapping("/recommendations/generated")
    public ResponseEntity<Page<Recommendation>> getGeneratedRecommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(insightService.getGeneratedRecommendations(PageRequest.of(page, Math.min(size, 100))));
    }

    @PostMapping("/recommendations/generate/{districtId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'ANALYST')")
    public ResponseEntity<OperationalInsightResponse> generateDistrictRecommendations(@PathVariable Long districtId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insightService.generateDistrictRecommendations(districtId));
    }

    @GetMapping("/insights/operational")
    public ResponseEntity<Page<OperationalInsightResponse>> getOperationalInsights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(insightService.generateAllRecommendations(PageRequest.of(page, Math.min(size, 50))));
    }

    @GetMapping("/insights/dashboard")
    public ResponseEntity<DashboardInsightResponse> getDashboardInsights() {
        return ResponseEntity.ok(insightService.generateDashboardInsights());
    }
}

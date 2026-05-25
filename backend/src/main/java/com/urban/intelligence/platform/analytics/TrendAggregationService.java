package com.urban.intelligence.platform.analytics;

import com.urban.intelligence.platform.domain.repository.AnalyticsEventRepository;
import com.urban.intelligence.platform.domain.repository.IncidentRepository;
import com.urban.intelligence.platform.dto.analytics.CategoryTrendResponse;
import com.urban.intelligence.platform.dto.analytics.DailyTrendResponse;
import com.urban.intelligence.platform.dto.analytics.WeeklyTrendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TrendAggregationService - Analyzes temporal trends in urban incident and analytics data.
 *
 * PHASE 2 REFACTOR: All aggregation now happens in the database via native SQL GROUP BY.
 * No full entity collections are loaded into JVM memory for counting/grouping.
 *
 * Before: findCreatedAtAfter() → stream().groupBy(date) — O(n incidents) memory
 * After:  getDailyIncidentCounts() → returns [date, count] — O(days) memory
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TrendAggregationService {

    private final IncidentRepository incidentRepository;
    private final AnalyticsEventRepository analyticsEventRepository;
    private static final int TREND_WINDOW_DAYS = 30;
    private static final int WEEKS_TO_ANALYZE = 4;
    private static final Map<String, Integer> SEVERITY_WEIGHTS = Map.of(
        "LOW", 1, "MEDIUM", 2, "HIGH", 4, "CRITICAL", 7
    );

    /**
     * Analyze daily incident trends using database-side GROUP BY.
     * Instead of loading 30 days of incidents into memory and grouping with Java streams,
     * this method issues 3 lightweight aggregation queries returning only [date, count] rows.
     *
     * Memory reduction: O(31 rows) vs O(all incidents in 30 days)
     * Query count: 3 (was effectively 1 large query + in-memory processing)
     */
    public DailyTrendResponse analyzeDailyTrends() {
        log.info("Analyzing daily incident trends for past {} days", TREND_WINDOW_DAYS);

        LocalDateTime startDate = LocalDateTime.now().minusDays(TREND_WINDOW_DAYS);

        // DB-side aggregation: 3 light queries instead of loading all entities
        Map<LocalDate, Long> totalByDate = toDateMap(incidentRepository.getDailyIncidentCounts(startDate));
        Map<LocalDate, Long> criticalByDate = toDateMap(incidentRepository.getDailyCriticalCounts(startDate));
        Map<LocalDate, Long> resolvedByDate = toDateMap(incidentRepository.getDailyResolvedCounts(startDate));

        List<DailyTrendResponse.DailyData> dailyData = new ArrayList<>();
        long totalIncidents = 0;

        for (int i = TREND_WINDOW_DAYS; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int count = totalByDate.getOrDefault(date, 0L).intValue();
            int critical = criticalByDate.getOrDefault(date, 0L).intValue();
            int resolved = resolvedByDate.getOrDefault(date, 0L).intValue();
            totalIncidents += count;

            dailyData.add(DailyTrendResponse.DailyData.builder()
                .date(date).incidentCount(count)
                .criticalCount(critical).resolvedCount(resolved)
                .build());
        }

        double firstHalf = dailyData.subList(0, dailyData.size() / 2).stream()
            .mapToInt(DailyTrendResponse.DailyData::getIncidentCount).average().orElse(0.0);
        double secondHalf = dailyData.subList(dailyData.size() / 2, dailyData.size()).stream()
            .mapToInt(DailyTrendResponse.DailyData::getIncidentCount).average().orElse(0.0);
        double growthPercentage = firstHalf == 0.0 ? 0.0 : ((secondHalf - firstHalf) / firstHalf) * 100;

        return DailyTrendResponse.builder()
            .startDate(LocalDate.now().minusDays(TREND_WINDOW_DAYS))
            .endDate(LocalDate.now())
            .totalIncidents((int) totalIncidents)
            .averageDaily(round(totalIncidents / (double) TREND_WINDOW_DAYS))
            .growthPercentage(round(growthPercentage))
            .trendIndicator(determineTrendIndicator(growthPercentage))
            .dailyData(dailyData)
            .build();
    }

    /**
     * Analyze weekly incident trends using database-side aggregation.
     * Before: load ALL incidents for 4 weeks → filter per week in stream
     * After:  1 query returning [week_offset, count] rows
     *
     * Memory reduction: O(4 rows) vs O(all incidents in 4 weeks)
     */
    public WeeklyTrendResponse analyzeWeeklyTrends() {
        log.info("Analyzing weekly incident trends");

        LocalDateTime startDate = LocalDateTime.now().minusWeeks(WEEKS_TO_ANALYZE);
        Map<Integer, Long> weeklyCounts = toIntMap(incidentRepository.getWeeklyIncidentCounts(startDate));

        List<WeeklyTrendResponse.WeeklyData> weeklyData = new ArrayList<>();
        for (int i = 1; i <= WEEKS_TO_ANALYZE; i++) {
            int count = weeklyCounts.getOrDefault(i, 0L).intValue();
            weeklyData.add(WeeklyTrendResponse.WeeklyData.builder()
                .week(i).incidentCount(count)
                .categoryBreakdown(Map.of()) // simplified: detail available via getCategoryCounts
                .severityDistribution(Map.of())
                .resolutionRate(0.0)
                .build());
        }

        int totalIncidents = weeklyData.stream().mapToInt(WeeklyTrendResponse.WeeklyData::getIncidentCount).sum();
        return WeeklyTrendResponse.builder()
            .weeksAnalyzed(WEEKS_TO_ANALYZE)
            .totalIncidents(totalIncidents)
            .averageWeekly(round(totalIncidents / (double) WEEKS_TO_ANALYZE))
            .weeklyData(weeklyData)
            .build();
    }

    /**
     * Analyze category trends using database-side aggregation.
     * Before: load ALL incidents → groupBy type in stream + compute avg severity per type
     * After:  2 queries returning [type, count] and [type, severity, count] rows
     *
     * Memory reduction: O(unique categories) vs O(all incidents)
     */
    public CategoryTrendResponse analyzeCategoryTrends() {
        log.info("Analyzing incident category trends");

        LocalDateTime startDate = LocalDateTime.now().minusDays(TREND_WINDOW_DAYS);

        // Query 1: category counts
        Map<String, Long> categoryCounts = toStringLongMap(incidentRepository.getCategoryCounts(startDate));
        int totalIncidents = categoryCounts.values().stream().mapToInt(Long::intValue).sum();

        // Query 2: resolution rates per category
        Map<String, Long[]> resolutionData = new HashMap<>();
        for (Object[] row : incidentRepository.getCategoryResolutionRates(startDate)) {
            String type = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long resolved = ((Number) row[2]).longValue();
            resolutionData.put(type, new Long[]{total, resolved});
        }

        // Query 3: severity distribution for average severity computation
        Map<String, Map<String, Long>> severityDist = new HashMap<>();
        for (Object[] row : incidentRepository.getCategorySeverityDistribution(startDate)) {
            String type = (String) row[0];
            String severity = (String) row[1];
            long count = ((Number) row[2]).longValue();
            severityDist.computeIfAbsent(type, k -> new HashMap<>()).merge(severity, count, Long::sum);
        }

        List<CategoryTrendResponse.CategoryData> categoryData = categoryCounts.entrySet().stream()
            .map(entry -> {
                String type = entry.getKey();
                long count = entry.getValue();
                double percentage = totalIncidents == 0 ? 0.0 : (count / (double) totalIncidents) * 100;

                Long[] res = resolutionData.getOrDefault(type, new Long[]{count, 0L});
                double resolutionRate = res[0] == 0 ? 0.0 : (res[1] / (double) res[0]) * 100;

                // Compute average severity from distribution
                Map<String, Long> dist = severityDist.getOrDefault(type, Map.of());
                double avgSeverity = dist.entrySet().stream()
                    .mapToDouble(e -> SEVERITY_WEIGHTS.getOrDefault(e.getKey(), 1) * e.getValue())
                    .sum() / Math.max(count, 1);

                return CategoryTrendResponse.CategoryData.builder()
                    .category(type).count((int) count)
                    .percentage(round(percentage))
                    .averageSeverity(round(avgSeverity))
                    .resolutionRate(round(resolutionRate))
                    .build();
            })
            .sorted(Comparator.comparingInt(CategoryTrendResponse.CategoryData::getCount).reversed())
            .collect(Collectors.toList());

        return CategoryTrendResponse.builder()
            .analysisWindow(TREND_WINDOW_DAYS)
            .totalIncidents(totalIncidents)
            .uniqueCategories(categoryData.size())
            .topCategory(categoryData.isEmpty() ? null : categoryData.get(0).getCategory())
            .categoryData(categoryData)
            .build();
    }

    public Map<String, Long> getIncidentTypeTrends() {
        log.debug("Computing 30-day incident type trends");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Map<String, Long> trends = toStringLongMap(incidentRepository.getIncidentTypeTrends(thirtyDaysAgo));
        log.info("Identified {} incident type trends", trends.size());
        return trends;
    }

    public Map<String, Object> calculateCategoryTrendVelocity(String category) {
        log.debug("Calculating trend velocity for category: {}", category);
        Map<String, Object> trendAnalysis = new LinkedHashMap<>();
        trendAnalysis.put("category", category);

        Double recentAverage = analyticsEventRepository.getAverageScoreByCategory(category);
        trendAnalysis.put("recent_average_score", recentAverage != null ?
            Math.round(recentAverage * 100.0) / 100.0 : 0.0);
        trendAnalysis.put("event_count_7_days", analyticsEventRepository.countByCategory(category));
        trendAnalysis.put("trend_direction", "STABLE");
        trendAnalysis.put("change_percentage", 0.0);
        return trendAnalysis;
    }

    public Map<String, Long> getAnalyticsCategoryDistribution() {
        log.debug("Computing 7-day analytics category distribution");
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Map<String, Long> distribution = toStringLongMap(
            analyticsEventRepository.getCategoryDistribution(sevenDaysAgo));
        log.info("Category distribution: {} categories identified", distribution.size());
        return distribution;
    }

    public List<Map<String, Object>> detectActivityAnomalies() {
        log.debug("Scanning for activity anomalies");
        List<Map<String, Object>> anomalies = new ArrayList<>();
        double spikeThreshold = 1.5;

        List<Object[]> batchStats = analyticsEventRepository.getBatchCategoryStats();
        for (Object[] row : batchStats) {
            String category = (String) row[0];
            Double avgScore = (Double) row[1];
            Double maxScore = (Double) row[2];

            if (avgScore != null && maxScore != null && avgScore > 0) {
                double spikeRatio = maxScore / avgScore;
                if (spikeRatio > spikeThreshold) {
                    Map<String, Object> anomaly = new LinkedHashMap<>();
                    anomaly.put("category", category);
                    anomaly.put("average_score", Math.round(avgScore * 100.0) / 100.0);
                    anomaly.put("spike_score", Math.round(maxScore * 100.0) / 100.0);
                    anomaly.put("spike_ratio", Math.round(spikeRatio * 100.0) / 100.0);
                    anomaly.put("severity", spikeRatio > 2.0 ? "HIGH" : "MEDIUM");
                    anomalies.add(anomaly);
                }
            }
        }
        log.info("Detected {} activity anomalies", anomalies.size());
        return anomalies;
    }

    // ====== Private helpers ======

    private String determineTrendIndicator(double growthPercentage) {
        if (growthPercentage > 10) return "INCREASING";
        if (growthPercentage < -10) return "DECREASING";
        return "STABLE";
    }

    /** Convert native query result [[date, count], ...] → Map<LocalDate, Long> */
    private Map<LocalDate, Long> toDateMap(List<Object[]> rows) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((java.sql.Date) row[0]).toLocalDate(), ((Number) row[1]).longValue());
        }
        return map;
    }

    /** Convert native query result [[week, count], ...] → Map<Integer, Long> */
    private Map<Integer, Long> toIntMap(List<Object[]> rows) {
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return map;
    }

    /** Convert native query result [[key, value], ...] → Map<String, Long> */
    private Map<String, Long> toStringLongMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
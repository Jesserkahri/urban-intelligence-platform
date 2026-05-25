package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Incident;
import com.urban.intelligence.platform.dto.analytics.DailyTrendResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IncidentRepository - Data access layer for Incident entities.
 *
 * Optimized queries: uses JPQL GROUP BY and projection DTOs
 * for analytics aggregation instead of in-memory Java streams.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Page<Incident> findByDistrict_Id(Long districtId, Pageable pageable);

    Page<Incident> findByStatus(Incident.IncidentStatus status, Pageable pageable);

    Page<Incident> findBySeverity(Incident.SeverityLevel severity, Pageable pageable);

    List<Incident> findByCreatedAtAfter(LocalDateTime timestamp);

    List<Incident> findByDistrict(District district);

    Long countByDistrict(District district);

    List<Incident> findByDistrictAndStatusIn(District district, List<Incident.IncidentStatus> statuses);

    List<Incident> findByDistrictAndType(District district, String type);

    List<Incident> findByDistrictAndSeverity(District district, Incident.SeverityLevel severity);

    List<Incident> findByDistrictAndCreatedAtBetween(District district, LocalDateTime startDate, LocalDateTime endDate);

    List<Incident> findByDistrictAndCreatedAtAfter(District district, LocalDateTime createdAt);

    long countByDistrictAndSeverity(District district, Incident.SeverityLevel severity);

    @Query("SELECT i FROM Incident i WHERE i.district.id = :districtId AND i.status != 'CLOSED' " +
           "ORDER BY i.severity DESC, i.createdAt DESC")
    List<Incident> findActiveIncidentsByDistrict(@Param("districtId") Long districtId);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.district.id = :districtId " +
           "AND i.createdAt >= :startDate")
    Long countIncidentsByDistrictAndDate(@Param("districtId") Long districtId,
                                         @Param("startDate") LocalDateTime startDate);

    @Query("SELECT i.type, COUNT(i) FROM Incident i " +
           "WHERE i.createdAt >= :startDate GROUP BY i.type")
    List<Object[]> getIncidentTypeTrends(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT DISTINCT i.type FROM Incident i")
    List<String> findAllIncidentTypes();

    // ====== Batch/optimized queries for analytics ======

    /**
     * Bulk incident count per district over a time period.
     * Avoids N+1 by returning all counts at once.
     */
    @Query("SELECT i.district.id, COUNT(i) FROM Incident i " +
           "WHERE i.createdAt >= :startDate GROUP BY i.district.id")
    List<Object[]> countIncidentsByDistrictGrouped(@Param("startDate") LocalDateTime startDate);

    /**
     * Load all incidents for a district in one query with JOIN FETCH.
     */
    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.district WHERE i.district = :district")
    List<Incident> findByDistrictWithFetch(@Param("district") District district);

    // ====== Database-side aggregation queries (Phase 2 refactor) ======
    // Replace in-memory Java stream grouping with JPQL GROUP BY

    /**
     * Daily incident counts over date range. Returns [date, count] rows.
     * Replaces: incidentRepository.findByCreatedAtAfter().stream().groupBy(date)
     * Memory: O(days) instead of O(incidents)
     */
    @Query(value = """
        SELECT CAST(i.created_at AS DATE) as date, COUNT(*) as cnt
        FROM incidents i
        WHERE i.created_at >= :startDate
        GROUP BY CAST(i.created_at AS DATE)
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getDailyIncidentCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Daily critical incident counts over date range.
     */
    @Query(value = """
        SELECT CAST(i.created_at AS DATE) as date, COUNT(*) as cnt
        FROM incidents i
        WHERE i.created_at >= :startDate AND i.severity = 'CRITICAL'
        GROUP BY CAST(i.created_at AS DATE)
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getDailyCriticalCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Daily resolved/closed incident counts over date range.
     */
    @Query(value = """
        SELECT CAST(i.created_at AS DATE) as date, COUNT(*) as cnt
        FROM incidents i
        WHERE i.created_at >= :startDate
          AND (i.status = 'RESOLVED' OR i.status = 'CLOSED')
        GROUP BY CAST(i.created_at AS DATE)
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getDailyResolvedCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Weekly incident counts. Returns [week_offset, count] rows.
     * Replaces: load all incidents → filter per week in stream
     * Memory: O(weeks) instead of O(incidents)
     */
    @Query(value = """
        SELECT
          EXTRACT(WEEK FROM i.created_at)::int - EXTRACT(WEEK FROM :startDate::timestamp)::int + 1 as week,
          COUNT(*) as cnt
        FROM incidents i
        WHERE i.created_at >= :startDate
        GROUP BY EXTRACT(WEEK FROM i.created_at)
        ORDER BY week
        """, nativeQuery = true)
    List<Object[]> getWeeklyIncidentCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Count incidents by type over date range (for category breakdown).
     * Replaces: in-memory groupingBy(Incident::getType)
     */
    @Query(value = """
        SELECT i.type, COUNT(*) as cnt
        FROM incidents i
        WHERE i.created_at >= :startDate
        GROUP BY i.type
        ORDER BY cnt DESC
        """, nativeQuery = true)
    List<Object[]> getCategoryCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Resolution rate per category: (resolved / total) per type.
     * Returns [type, total, resolved] for each category.
     */
    @Query(value = """
        SELECT i.type,
               COUNT(*) as total,
               SUM(CASE WHEN i.status = 'RESOLVED' OR i.status = 'CLOSED' THEN 1 ELSE 0 END) as resolved
        FROM incidents i
        WHERE i.created_at >= :startDate
        GROUP BY i.type
        """, nativeQuery = true)
    List<Object[]> getCategoryResolutionRates(@Param("startDate") LocalDateTime startDate);

    /**
     * Severity count distribution per type.
     * Returns [type, severity, count] for GROUP BY on two columns.
     * Used to compute average severity weight per category without loading full entities.
     */
    @Query(value = """
        SELECT i.type, i.severity, COUNT(*) as cnt
        FROM incidents i
        WHERE i.created_at >= :startDate
        GROUP BY i.type, i.severity
        ORDER BY i.type, i.severity
        """, nativeQuery = true)
    List<Object[]> getCategorySeverityDistribution(@Param("startDate") LocalDateTime startDate);
}
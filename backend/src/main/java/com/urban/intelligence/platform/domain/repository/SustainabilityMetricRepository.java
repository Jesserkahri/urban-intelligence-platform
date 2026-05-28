package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.SustainabilityMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SustainabilityMetricRepository - Data access for sustainability metrics
 */
@Repository
public interface SustainabilityMetricRepository extends JpaRepository<SustainabilityMetric, Long> {

    Page<SustainabilityMetric> findByDistrict_Id(Long districtId, Pageable pageable);

    Page<SustainabilityMetric> findByMetricType(String metricType, Pageable pageable);

    Page<SustainabilityMetric> findByStatus(String status, Pageable pageable);

    List<SustainabilityMetric> findByDistrict_IdAndMetricType(Long districtId, String metricType);

    @Query("SELECT sm FROM SustainabilityMetric sm WHERE sm.district.id = :districtId " +
            "AND sm.metricType = :metricType ORDER BY sm.timestamp DESC LIMIT 1")
    SustainabilityMetric findLatestByDistrictAndType(
            @Param("districtId") Long districtId,
            @Param("metricType") String metricType);

    @Query("SELECT sm FROM SustainabilityMetric sm WHERE sm.status = 'CRITICAL' " +
            "ORDER BY sm.timestamp DESC")
    List<SustainabilityMetric> findCriticalMetrics();

    @Query("SELECT sm FROM SustainabilityMetric sm WHERE sm.district.id = :districtId " +
            "AND sm.timestamp >= :since ORDER BY sm.timestamp DESC")
    List<SustainabilityMetric> findRecentMetrics(
            @Param("districtId") Long districtId,
            @Param("since") LocalDateTime since);

    long countByStatus(String status);
}

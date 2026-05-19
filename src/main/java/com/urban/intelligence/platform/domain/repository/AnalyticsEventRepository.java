package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.AnalyticsEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AnalyticsEventRepository - Data access layer for AnalyticsEvent entities
 */
@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

    Page<AnalyticsEvent> findByCategory(String category, Pageable pageable);

    Page<AnalyticsEvent> findBySource(String source, Pageable pageable);

    List<AnalyticsEvent> findByTimestampAfter(LocalDateTime timestamp);

    List<AnalyticsEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(ae.score) FROM AnalyticsEvent ae WHERE ae.category = :category")
    Double getAverageScoreByCategory(@Param("category") String category);

    @Query("SELECT MAX(ae.score) FROM AnalyticsEvent ae WHERE ae.category = :category")
    Double getMaxScoreByCategory(@Param("category") String category);

    @Query("SELECT MIN(ae.score) FROM AnalyticsEvent ae WHERE ae.category = :category")
    Double getMinScoreByCategory(@Param("category") String category);

    @Query("SELECT COUNT(ae) FROM AnalyticsEvent ae WHERE ae.category = :category")
    Long countByCategory(@Param("category") String category);

    @Query("SELECT ae.category, COUNT(ae) FROM AnalyticsEvent ae " +
           "WHERE ae.timestamp >= :startDate GROUP BY ae.category")
    List<Object[]> getCategoryDistribution(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT DISTINCT ae.category FROM AnalyticsEvent ae")
    List<String> findAllCategories();

    @Query("SELECT ae FROM AnalyticsEvent ae WHERE ae.timestamp >= :timestamp " +
           "ORDER BY ae.score DESC")
    List<AnalyticsEvent> findRecentHighScoringEvents(@Param("timestamp") LocalDateTime timestamp);
}

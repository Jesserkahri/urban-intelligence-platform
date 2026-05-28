package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.SustainabilityScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SustainabilityScoreRepository - Data access for district sustainability ratings
 */
@Repository
public interface SustainabilityScoreRepository extends JpaRepository<SustainabilityScore, Long> {

    Optional<SustainabilityScore> findByDistrictIdOrderByCalculatedAtDesc(Long districtId);

    Page<SustainabilityScore> findByRating(String rating, Pageable pageable);

    Page<SustainabilityScore> findByTrend(String trend, Pageable pageable);

    @Query("SELECT ss FROM SustainabilityScore ss ORDER BY ss.overallScore DESC")
    List<SustainabilityScore> findAllOrderByScoreDesc();

    @Query("SELECT ss FROM SustainabilityScore ss WHERE ss.trend = 'IMPROVING' " +
            "ORDER BY ss.trendPercentage DESC")
    List<SustainabilityScore> findImprovingDistricts();

    @Query("SELECT ss FROM SustainabilityScore ss WHERE ss.trend = 'DECLINING' " +
            "ORDER BY ss.trendPercentage ASC")
    List<SustainabilityScore> findDecliningDistricts();

    @Query("SELECT AVG(ss.overallScore) FROM SustainabilityScore ss")
    Double getAverageOverallScore();

    @Query("SELECT AVG(ss.environmentalScore) FROM SustainabilityScore ss")
    Double getAverageEnvironmentalScore();

    @Query("SELECT COUNT(ss) FROM SustainabilityScore ss WHERE ss.rating = :rating")
    long countByRating(@Param("rating") String rating);
}

package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DistrictRepository - Data access layer for District entities
 */
@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    Optional<District> findByName(String name);

    @Query("SELECT d FROM District d ORDER BY d.operationalRiskScore DESC")
    List<District> findByHighestRisk();

    @Query("SELECT d FROM District d WHERE d.sustainabilityScore < :threshold " +
           "ORDER BY d.sustainabilityScore ASC")
    List<District> findBelowSustainabilityThreshold(@Param("threshold") Double threshold);

    @Query("SELECT d FROM District d WHERE d.operationalRiskScore > :threshold " +
           "ORDER BY d.operationalRiskScore DESC")
    List<District> findAboveRiskThreshold(@Param("threshold") Double threshold);

    @Query("SELECT AVG(d.operationalRiskScore) FROM District d")
    Double getAverageOperationalRiskScore();

    @Query("SELECT AVG(d.sustainabilityScore) FROM District d")
    Double getAverageSustainabilityScore();

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.district = :district")
    Long getIncidentCount(@Param("district") District district);
}

package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.District;
import com.urban.intelligence.platform.domain.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IncidentRepository - Data access layer for Incident entities
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
}
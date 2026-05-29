package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.PredictiveAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PredictiveAlertRepository extends JpaRepository<PredictiveAlert, Long> {
    Page<PredictiveAlert> findBySeverity(String severity, Pageable pageable);
    Page<PredictiveAlert> findByDistrict_Id(Long districtId, Pageable pageable);
    List<PredictiveAlert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime createdAt);
}

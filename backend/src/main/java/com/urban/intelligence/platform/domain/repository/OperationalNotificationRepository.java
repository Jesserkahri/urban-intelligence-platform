package com.urban.intelligence.platform.domain.repository;

import com.urban.intelligence.platform.domain.entity.OperationalNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationalNotificationRepository extends JpaRepository<OperationalNotification, Long> {
    Page<OperationalNotification> findByAcknowledgedFalse(Pageable pageable);
    Page<OperationalNotification> findBySeverity(String severity, Pageable pageable);
    List<OperationalNotification> findTop20ByOrderByCreatedAtDesc();
    long countByAcknowledgedFalse();
    long countBySeverityAndCreatedAtAfter(String severity, LocalDateTime createdAt);
}

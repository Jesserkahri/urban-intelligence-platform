package com.urban.intelligence.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Incident Entity - Represents urban incidents (accidents, disruptions, etc.)
 * 
 * Tracks events across the city with spatial and temporal information
 * for analysis and response coordination.
 */
@Entity
@Table(name = "incidents", indexes = {
    @Index(name = "idx_incident_district", columnList = "district_id"),
    @Index(name = "idx_incident_status", columnList = "status"),
    @Index(name = "idx_incident_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severity;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SeverityLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum IncidentStatus {
        REPORTED, IN_PROGRESS, RESOLVED, CLOSED
    }
}

package com.urban.intelligence.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * AnalyticsEvent Entity - Represents data points for analytics processing
 * 
 * Captures raw event data from various sources for aggregation, trending,
 * and predictive analytics pipeline.
 */
@Entity
@Table(name = "analytics_events", indexes = {
    @Index(name = "idx_analytics_category", columnList = "category"),
    @Index(name = "idx_analytics_source", columnList = "source"),
    @Index(name = "idx_analytics_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

package com.urban.intelligence.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * SustainabilityMetric Entity - Environmental and sustainability measurements.
 */
@Entity
@Table(name = "sustainability_metrics", indexes = {
    @Index(name = "idx_sustainability_district", columnList = "district_id"),
    @Index(name = "idx_sustainability_metric_type", columnList = "metric_type"),
    @Index(name = "idx_sustainability_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(nullable = false, length = 100)
    private String metricType;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Double threshold;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String source;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updateStatus();
    }

    private void updateStatus() {
        if (value == null || threshold == null || threshold <= 0) {
            this.status = "MODERATE";
            return;
        }

        boolean higherIsBetter = metricType != null && (
            metricType.equals("GREEN_SPACE") ||
            metricType.equals("RECYCLING_RATE") ||
            metricType.equals("TRANSIT_EFFICIENCY") ||
            metricType.equals("MOBILITY_FLOW") ||
            metricType.equals("RENEWABLE_ENERGY")
        );

        double ratio = value / threshold;
        if (higherIsBetter) {
            if (ratio >= 1.0) this.status = "GOOD";
            else if (ratio >= 0.75) this.status = "MODERATE";
            else if (ratio >= 0.5) this.status = "POOR";
            else this.status = "CRITICAL";
            return;
        }

        if (ratio <= 0.5) this.status = "GOOD";
        else if (ratio <= 0.75) this.status = "MODERATE";
        else if (ratio <= 1.0) this.status = "POOR";
        else this.status = "CRITICAL";
    }
}

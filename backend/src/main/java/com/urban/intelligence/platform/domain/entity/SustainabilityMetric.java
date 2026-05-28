package com.urban.intelligence.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * SustainabilityMetric Entity - Environmental and sustainability measurements
 *
 * Tracks key sustainability indicators:
 * - Air quality indices
 * - Emissions levels
 * - Waste management metrics
 * - Energy consumption
 * - Water usage
 * - Green space coverage
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
    private String metricType; // AIR_QUALITY, EMISSIONS, WASTE, ENERGY, WATER, GREEN_SPACE

    @Column(nullable = false)
    private Double value; // Current metric value

    @Column(nullable = false)
    private String unit; // µg/m³, kg CO2e, tons, kWh, m³, %

    @Column(nullable = false)
    private Double threshold; // Critical threshold for alert

    @Column(nullable = false)
    private String status; // GOOD, MODERATE, POOR, CRITICAL

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String source; // Sensor ID, API source, calculation method

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Status determined by comparing value to threshold
        if (value <= threshold * 0.5) {
            this.status = "GOOD";
        } else if (value <= threshold * 0.75) {
            this.status = "MODERATE";
        } else if (value <= threshold) {
            this.status = "POOR";
        } else {
            this.status = "CRITICAL";
        }
    }
}

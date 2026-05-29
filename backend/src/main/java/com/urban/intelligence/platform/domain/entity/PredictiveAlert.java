package com.urban.intelligence.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "predictive_alerts", indexes = {
    @Index(name = "idx_predictive_alert_district", columnList = "district_id"),
    @Index(name = "idx_predictive_alert_type", columnList = "alert_type"),
    @Index(name = "idx_predictive_alert_severity", columnList = "severity"),
    @Index(name = "idx_predictive_alert_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictiveAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Column(name = "alert_type", nullable = false, length = 80)
    private String alertType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "probability", nullable = false)
    private Double probability;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "forecast_window_days", nullable = false)
    private Integer forecastWindowDays;

    @Column(name = "predicted_value")
    private Double predictedValue;

    @Column(name = "baseline_value")
    private Double baselineValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

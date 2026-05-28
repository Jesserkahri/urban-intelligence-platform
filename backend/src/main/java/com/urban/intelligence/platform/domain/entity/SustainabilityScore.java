package com.urban.intelligence.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * SustainabilityScore Entity - Overall district sustainability ratings
 *
 * Aggregated sustainability performance index combining:
 * - Environmental metrics
 * - Mobility efficiency
 * - Energy sustainability
 * - Waste management
 * - Social equity indicators
 */
@Entity
@Table(name = "sustainability_scores", indexes = {
    @Index(name = "idx_score_district", columnList = "district_id"),
    @Index(name = "idx_score_timestamp", columnList = "calculated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SustainabilityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(nullable = false)
    private Double overallScore; // 0-100

    @Column(nullable = false)
    private Double environmentalScore; // 0-100

    @Column(nullable = false)
    private Double mobilityScore; // 0-100

    @Column(nullable = false)
    private Double energyScore; // 0-100

    @Column(nullable = false)
    private Double wasteScore; // 0-100

    @Column(nullable = false)
    private String rating; // A, B, C, D, F

    @Column(nullable = false)
    private String trend; // IMPROVING, STABLE, DECLINING

    @Column(nullable = false)
    private Double trendPercentage; // Month-over-month change

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @Column(nullable = false)
    private LocalDateTime previousCalculation;

    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
        if (previousCalculation == null) {
            previousCalculation = calculatedAt;
        }
        updateRating();
    }

    @PreUpdate
    protected void onUpdate() {
        previousCalculation = calculatedAt;
        calculatedAt = LocalDateTime.now();
        updateRating();
    }

    private void updateRating() {
        if (overallScore >= 90) this.rating = "A";
        else if (overallScore >= 80) this.rating = "B";
        else if (overallScore >= 70) this.rating = "C";
        else if (overallScore >= 60) this.rating = "D";
        else this.rating = "F";
    }
}

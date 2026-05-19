package com.urban.intelligence.platform.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * District Entity - Represents urban administrative districts
 * 
 * Contains metadata and key metrics for district-level analysis,
 * including sustainability and operational risk scores.
 */
@Entity
@Table(name = "districts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_district_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer population;

    @Column(nullable = false)
    private Double sustainabilityScore;

    @Column(nullable = false)
    private Double operationalRiskScore;

@Builder.Default
@OneToMany(mappedBy = "district", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Incident> incidents = new HashSet<>();

@Builder.Default
@OneToMany(mappedBy = "district", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Recommendation> recommendations = new HashSet<>();

    public void addIncident(Incident incident) {
        incidents.add(incident);
        incident.setDistrict(this);
    }

    public void removeIncident(Incident incident) {
        incidents.remove(incident);
        incident.setDistrict(null);
    }

    public void addRecommendation(Recommendation recommendation) {
        recommendations.add(recommendation);
        recommendation.setDistrict(this);
    }

    public void removeRecommendation(Recommendation recommendation) {
        recommendations.remove(recommendation);
        recommendation.setDistrict(null);
    }
}

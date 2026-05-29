package com.urban.intelligence.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentAssignmentRequest {
    @NotBlank
    private String assignedTo;
}

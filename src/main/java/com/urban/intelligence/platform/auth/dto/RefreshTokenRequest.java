package com.urban.intelligence.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * RefreshTokenRequest - DTO for token refresh requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
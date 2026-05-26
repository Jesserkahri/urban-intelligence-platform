package com.urban.intelligence.platform.auth.dto;

import lombok.*;

/**
 * TokenResponse - DTO for JWT token pair response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;
}
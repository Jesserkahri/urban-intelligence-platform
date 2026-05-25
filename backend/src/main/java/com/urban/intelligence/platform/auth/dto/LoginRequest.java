package com.urban.intelligence.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * LoginRequest - DTO for user authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String login;

    @NotBlank(message = "Password is required")
    private String password;
}
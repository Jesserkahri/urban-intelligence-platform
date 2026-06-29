package com.urban.intelligence.platform.auth.dto;

import com.urban.intelligence.platform.auth.domain.Role;
import com.urban.intelligence.platform.auth.domain.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;
import java.util.List;

/**
 * UserResponse - safe projection of User data for API responses.
 * Excludes sensitive fields like password, tokens, etc.
 */
@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String displayName;
    private Role role;
    private boolean enabled;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .role(user.getRole())
            .enabled(user.isEnabled())
            .emailVerified(user.isEmailVerified())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
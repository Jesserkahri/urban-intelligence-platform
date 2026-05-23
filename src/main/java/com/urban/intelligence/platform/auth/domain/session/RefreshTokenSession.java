package com.urban.intelligence.platform.auth.domain.session;

import com.urban.intelligence.platform.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * RefreshTokenSession - represents an active user session backed by a refresh token.
 *
 * Enables multi-session (multi-device) support. Each session stores its own
 * refresh token, device info, and revocation status. Simple and lightweight.
 */
@Entity
@Table(name = "refresh_token_sessions", indexes = {
    @Index(name = "idx_session_token", columnList = "token"),
    @Index(name = "idx_session_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(length = 255)
    private String deviceInfo;

    @Column(nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant revokedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
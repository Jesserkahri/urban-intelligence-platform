package com.urban.intelligence.platform.auth.repository;

import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.domain.session.RefreshTokenSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * RefreshTokenSessionRepository - data access for refresh token sessions.
 */
@Repository
public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {

    Optional<RefreshTokenSession> findByToken(String token);

    List<RefreshTokenSession> findByUserAndRevokedFalse(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshTokenSession s WHERE s.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") Instant now);
}
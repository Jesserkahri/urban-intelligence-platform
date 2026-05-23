package com.urban.intelligence.platform.auth.repository;

import com.urban.intelligence.platform.auth.domain.User;
import com.urban.intelligence.platform.auth.domain.session.RefreshTokenSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
package org.sopt.sopkathon.auth.repository;

import java.util.Optional;
import org.sopt.sopkathon.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    void deleteByMemberId(Long memberId);
}

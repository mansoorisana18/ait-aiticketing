package com.aiticketing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiticketing.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserId(Long userId); 
    Optional<RefreshToken> findByTokenHash(String tokenHash);

}
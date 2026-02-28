package com.aiticketing.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.entity.RefreshToken;
import com.aiticketing.entity.User;
import com.aiticketing.exception.UnauthorizedException;
import com.aiticketing.repository.RefreshTokenRepository;
import com.aiticketing.repository.UserRepository;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepo;
    private final UserRepository userRepo;
    private final long refreshExpMinutes;

    private final SecureRandom secureRandom = new SecureRandom();
    
    private static final Logger REFRESH_TOKEN_SERVICE_LOG = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepo,
            UserRepository userRepo,
            @Value("${app.security.refresh.expMinutes}") long refreshExpMinutes //7 days
    ) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.userRepo = userRepo;
        this.refreshExpMinutes = refreshExpMinutes;
    }

    @Transactional
    public String issueRefreshToken(User user) {
    	REFRESH_TOKEN_SERVICE_LOG.info("RefreshTokenServiceImpl :: in issueRefreshToken()");
        String raw = generateSecureToken();
        REFRESH_TOKEN_SERVICE_LOG.info("RefreshTokenServiceImpl :: in issueRefreshToken() :: raw={}", raw);
        String hash = sha256(raw);

        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(refreshExpMinutes);

        RefreshToken rt = refreshTokenRepo.findByUserId(user.getUserId())
                .orElseGet(RefreshToken::new);

        rt.setUserId(user.getUserId());
        rt.setTokenHash(hash);
        rt.setExpiresAt(expiresAt);
        rt.setRevoked(false);

        refreshTokenRepo.save(rt);
        REFRESH_TOKEN_SERVICE_LOG.info("RefreshTokenServiceImpl :: exit issueRefreshToken()");
        return raw;
    }

    @Transactional(readOnly = true)
    public User validateRefreshTokenOrThrow(String rawRefreshToken) {
    	REFRESH_TOKEN_SERVICE_LOG.info("RefreshTokenServiceImpl :: in validateRefreshTokenOrThrow()");
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Unauthorized");
        }

        String hash = sha256(rawRefreshToken);

        RefreshToken rt = refreshTokenRepo.findByTokenHash(hash)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException("Unauthorized");
        }
        
        REFRESH_TOKEN_SERVICE_LOG.info("RefreshTokenServiceImpl :: exit validateRefreshTokenOrThrow()");
        return userRepo.findById(rt.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
        
    }

    @Transactional
    public void revokeForUser(Long userId) {
    	REFRESH_TOKEN_SERVICE_LOG.info("RefreshTokenServiceImpl :: in revokeForUser()");
        refreshTokenRepo.findByUserId(userId).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepo.save(rt);
        });
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64]; //512 bits
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String raw) {
    	REFRESH_TOKEN_SERVICE_LOG.info("RefreshTokenServiceImpl :: in sha256()");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash refresh token", e);
        }
    }
}
package com.aiticketing.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aiticketing.entity.enums.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final byte[] secretBytes;
    private final long expMillis;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expMinutes}") long expMinutes
    ) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expMillis = expMinutes * 60_000L;
    }
    
    private static final Logger JWT_SERVICE_LOG = LoggerFactory.getLogger(JwtService.class);

    public String generateToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMillis);

        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(exp)
                .signWith(Keys.hmacShaKeyFor(secretBytes))
                .compact();
    }

    public Claims parseClaimsAndValidate(String token) {
        
    	Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretBytes))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    	
    	JWT_SERVICE_LOG.info("JwtService :: in parseClaimsAndValidate() :: JWT Claims :: subject(email)={} uid={} role={} issuedAt={} expiration={}",
    			claims.getSubject(),
    			claims.get("uid"),
    			claims.get("role"),
    			claims.getIssuedAt(),
    			claims.getExpiration()
    	);
    	return claims;
    }
 
}
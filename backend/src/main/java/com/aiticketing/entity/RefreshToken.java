package com.aiticketing.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rt_id")
    private Long id;

    @Column(name = "rt_user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "rt_token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "rt_expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "rt_revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "rt_created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "rt_updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() { 
    	return id; 
    }

    public Long getUserId() { 
    	return userId; 
    }
    
    public void setUserId(Long userId) { 
    	this.userId = userId; 
    }

    public String getTokenHash() { 
    	return tokenHash; 
    }
    
    public void setTokenHash(String tokenHash) { 
    	this.tokenHash = tokenHash; 
    }

    public OffsetDateTime getExpiresAt() { 
    	return expiresAt; 
    }
    
    public void setExpiresAt(OffsetDateTime expiresAt) { 
    	this.expiresAt = expiresAt; 
    }

    public boolean isRevoked() { 
    	return revoked; 
    }
    
    public void setRevoked(boolean revoked) { 
    	this.revoked = revoked; 
    }

    public OffsetDateTime getCreatedAt() { 
    	return createdAt; 
    }
    
    public OffsetDateTime getUpdatedAt() { 
    	return updatedAt; 
    }
}
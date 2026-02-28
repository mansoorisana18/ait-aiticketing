package com.aiticketing.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.aiticketing.entity.UserRole;

public class AuthUserPrincipal implements UserDetails {
	
	private final Long userId;
    private final String email;
    private final String passwordHash;
    private final UserRole role;

    public AuthUserPrincipal(Long userId, String email, String passwordHash, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getUserId() {
    	return userId; 
    }

    public UserRole role() { 
    	return role; 
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //ROLE_ prefix expected by Spring
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override 
    public String getPassword() { 
    	return passwordHash; 
    }
    
    @Override 
    public String getUsername() { 
    	return email; 
    }

    @Override 
    public boolean isAccountNonExpired() { 
    	return true; 
    }
    
    @Override 
    public boolean isAccountNonLocked() { 
    	return true; 
    }
    
    @Override 
    public boolean isCredentialsNonExpired() { 
    	return true; 
    }
    
    @Override 
    public boolean isEnabled() { 
    	return true; 
    }
}
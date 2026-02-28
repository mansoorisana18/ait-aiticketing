package com.aiticketing.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.controller.TicketController;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	public static final String JWT_AUTH_ERROR_ATTR = "JWT_AUTH_ERROR";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    
    private static final Logger JWT_FILTER_LOG = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        //Skip JWT parsing for public & swagger endpoints
        return path.equals("/api/users/login")
                || path.equals("/api/users/register")
                || path.equals("/api/users/refresh")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        JWT_FILTER_LOG.info("JwtAuthFilter :: in doFilterInternal() :: header={}",header);
        //if no JWT is provided then continue as spring security will decide if endpoint requires auth
        if (header == null || !header.startsWith("Bearer ")) {
        	JWT_FILTER_LOG.info("JwtAuthFilter :: in doFilterInternal() :: header not provided or doesnt have Bearer");
        	chain.doFilter(request, response);
            return;
        }
        
        String token = header.substring(7).trim();

        try {
            Claims claims = jwtService.parseClaimsAndValidate(token);
            String email = claims.getSubject();

            AuthUserPrincipal userDetails = (AuthUserPrincipal) userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
            
            

        } catch (Exception e) {
        	JWT_FILTER_LOG.info("JwtAuthFilter :: Exception in doFilterInternal() :: Expired/Invalid JWT");
        	//Marking that a JWT was present but it was invalid/expired this will be passed to springsecuirty that will use apiauthentrypoint to see if this flag is set then it will return invalid token message
            request.setAttribute(JWT_AUTH_ERROR_ATTR, "INVALID_OR_EXPIRED");
        	SecurityContextHolder.clearContext();          
        }
        chain.doFilter(request, response);
    }
}
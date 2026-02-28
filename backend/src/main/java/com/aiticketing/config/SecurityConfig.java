package com.aiticketing.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.aiticketing.security.ApiAccessDeniedHandler;
import com.aiticketing.security.ApiAuthEntryPoint;
import com.aiticketing.security.JwtAuthFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	
	@Value("${frontend.url}")
	String frontendUrl;
	
	@Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
	@Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, ApiAuthEntryPoint apiAuthEntryPoint,
            ApiAccessDeniedHandler apiAccessDeniedHandler) throws Exception {
		http
	        .csrf(csrf -> csrf.disable())	
	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        
	        .exceptionHandling(ex -> ex
	            .authenticationEntryPoint(apiAuthEntryPoint)   //401 JSON ApiResponseBean
	            .accessDeniedHandler(apiAccessDeniedHandler)   //403 JSON ApiResponseBean
	        )
	        
	        .authorizeHttpRequests(auth -> auth
	        		//Public endpoints allowed w/o JWT
	        		.requestMatchers("/api/users/register", "/api/users/login").permitAll()
	                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
	                .requestMatchers(HttpMethod.POST, "/api/users/refresh").permitAll()
	                
	                //Logout requires auth so that we can revoke that users refresh token
	                .requestMatchers(HttpMethod.POST, "/api/users/logout").authenticated()
	                
	                //admin endpoints
	                .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
	                .requestMatchers("/api/tickets/admin/**").hasRole("ADMIN")
	
	                //agent endpoint
	                .requestMatchers("/api/tickets/agent/**").hasAnyRole("AGENT", "ADMIN")
	
	                //Ticket endpoints only for authenticated users
	                .requestMatchers(HttpMethod.POST, "/api/tickets").hasAnyRole("USER", "ADMIN", "AGENT")
	                .requestMatchers(HttpMethod.GET, "/api/tickets").hasAnyRole("USER", "ADMIN", "AGENT")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/user/**").hasRole("USER")
	
//	                // Comments endpoint allow authenticated roles. service will filter/validate visibility
//	                .requestMatchers("/api/tickets/*/comments/**").authenticated()
	
	                //Default every other requests requires auth
	                .anyRequest().authenticated()
            )
//            .httpBasic(Customizer.withDefaults())
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
    }

	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(frontendUrl));
        cfg.setAllowedMethods(List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}

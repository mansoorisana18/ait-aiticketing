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
	                
	                //metrics
	                .requestMatchers(HttpMethod.GET, "/api/metrics/admin/**").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/metrics/agent/**").hasAnyRole("AGENT", "ADMIN")
	                
	                //admin user endpoints
	                .requestMatchers(HttpMethod.GET, "/api/users/admin").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.PATCH, "/api/users/admin/**").hasRole("ADMIN")

	                //admin ticket endpoints
	                .requestMatchers(HttpMethod.GET, "/api/tickets/admin/**").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/admin/override").hasRole("ADMIN")

	                //agent + admin internal ticket endpoints
	                .requestMatchers(HttpMethod.GET, "/api/tickets/agent").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/agent/**").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/agent/status").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/*/history").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/*/confirmed-duplicates").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/*/primary-link").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/*").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/*/eligible-agents").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.POST, "/api/tickets/agent/*/kb/manual-suggestion").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.POST, "/api/tickets/agent/*/kb-draft/generate").hasAnyRole("AGENT", "ADMIN")
	                
	                //user scoped ticket endpoints
	                .requestMatchers(HttpMethod.GET, "/api/tickets/user/**").hasRole("USER")
	                .requestMatchers(HttpMethod.PATCH, "/api/tickets/user/*/clarify").hasRole("USER")
	                .requestMatchers(HttpMethod.POST, "/api/tickets/user/*/kb-response").hasRole("USER")
	                
	                //Ticket endpoints only for authenticated users
	                .requestMatchers(HttpMethod.POST, "/api/tickets").hasAnyRole("USER", "AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets").hasAnyRole("USER", "AGENT", "ADMIN")

	                //Comments for authenticated roles, service layer enforces visibility and ownership
	                .requestMatchers(HttpMethod.POST, "/api/tickets/*/comments").hasAnyRole("USER", "AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/tickets/*/comments").hasAnyRole("USER", "AGENT", "ADMIN")
	
	                //KB endpoints
	                .requestMatchers(HttpMethod.POST, "/api/kb/admin").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.PUT, "/api/kb/admin/**").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/kb/admin").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/kb/*").hasAnyRole("USER", "AGENT", "ADMIN")
//	                .requestMatchers(HttpMethod.GET, "/api/kb").hasAnyRole("AGENT", "ADMIN")
	                
	                //KB draft workflow
	                .requestMatchers(HttpMethod.PUT, "/api/kb/agent/*/draft").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.POST, "/api/kb/agent/*/submit-review").hasAnyRole("AGENT", "ADMIN")
	                .requestMatchers(HttpMethod.GET, "/api/kb/admin/review").hasRole("ADMIN")
	                .requestMatchers(HttpMethod.POST, "/api/kb/admin/*/review-decision").hasRole("ADMIN")
	                
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

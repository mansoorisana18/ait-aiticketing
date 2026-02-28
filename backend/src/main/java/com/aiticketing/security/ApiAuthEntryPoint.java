package com.aiticketing.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.aiticketing.bean.response.ApiResponseBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, JsonProcessingException, java.io.IOException {

    	String msg = "Unauthorized";

    	//If the request had JWT and was set expired by jwtauthfilter then return expired message
        Object jwtErr = request.getAttribute(JwtAuthFilter.JWT_AUTH_ERROR_ATTR);
        if (jwtErr != null) {
            msg = "Invalid or expired token";
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        ApiResponseBean<Object> body = ApiResponseBean.failure(msg);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

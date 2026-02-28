package com.aiticketing.config;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.bean.response.ValidationError;
import com.aiticketing.exception.BadRequestException;
import com.aiticketing.exception.ConflictException;
import com.aiticketing.exception.NotFoundException;
import com.aiticketing.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger EXCEPTION_HANDLER_LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	//Validation errors from @Valid on request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleValidation(MethodArgumentNotValidException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleValidation()");
    	List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
            .collect(Collectors.toList());
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: exit handleValidation() :: errors={}",errors);
        return ResponseEntity.badRequest().body(ApiResponseBean.failure("Validation failed",
                errors));
    }

    //Constraint violations (like @RequestParam validation)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleConstraintViolation(ConstraintViolationException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleConstraintViolation()");
    	List<ValidationError> errors = ex.getConstraintViolations().stream()
            .map(cv -> new ValidationError(
                    cv.getPropertyPath().toString(),
                    cv.getMessage()
            ))
            .collect(Collectors.toList());
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: exit handleConstraintViolation():: errors={}",errors);
        return ResponseEntity.badRequest().body(ApiResponseBean.failure("Validation failed",
                errors));
    }

    //Bad JSON, wrong format, etc.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleUnreadable(HttpMessageNotReadableException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleUnreadable()");
    	return ResponseEntity.badRequest()
                .body(ApiResponseBean.failure("Malformed JSON request"));
    }
    
    ///tickets/{id} where id is not a number, etc.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleTypeMismatch()");
    	return ResponseEntity.badRequest()
                .body(ApiResponseBean.failure("Invalid parameter: " + ex.getName()));
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleConflict(ConflictException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleConflict()");
    	return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseBean.failure(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleNotFound(NotFoundException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleNotFound()");
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseBean.failure(ex.getMessage()));
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleDenied(AccessDeniedException ex) {
    	EXCEPTION_HANDLER_LOG.warn("GlobalExceptionHandler :: in handleDenied() :: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseBean.failure("Forbidden"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleAuth(AuthenticationException ex) {
    	EXCEPTION_HANDLER_LOG.warn("GlobalExceptionHandler :: in handleAuth() :: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseBean.failure("Unauthorized"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleUnauthorized(UnauthorizedException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleUnauthorized()");
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseBean.failure(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseBean<Object>> handleBadRequest(BadRequestException ex) {
    	EXCEPTION_HANDLER_LOG.info("GlobalExceptionHandler :: in handleBadRequest()");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseBean.failure(ex.getMessage()));
    }

    //fallback - internal server error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseBean<Object>> handleAny(Exception ex, HttpServletRequest req) {
    	EXCEPTION_HANDLER_LOG.error("GlobalExceptionHandler :: in handleAny(): {} {}",
                 req.getMethod(),
                 req.getRequestURI(),
                 ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseBean.failure("Internal server error"));
    }
}

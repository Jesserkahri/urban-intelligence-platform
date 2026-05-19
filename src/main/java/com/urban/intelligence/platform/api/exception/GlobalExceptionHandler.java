package com.urban.intelligence.platform.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * GlobalExceptionHandler - Centralized exception handling for all REST endpoints
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        ApiError apiError = ApiError.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .error("NOT_FOUND")
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle validation errors from request body validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Validation error: {}", ex.getMessage());
        
        ApiError apiError = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .error("VALIDATION_ERROR")
            .timestamp(LocalDateTime.now())
            .build();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            apiError.addValidationError(fieldName, errorMessage);
        });

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ApiError apiError = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .error("INVALID_ARGUMENT")
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ApiError apiError = ApiError.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("An unexpected error occurred")
            .error("INTERNAL_SERVER_ERROR")
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

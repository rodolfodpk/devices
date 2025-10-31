package com.rdpk.config;

import com.rdpk.device.dto.ErrorResponse;
import com.rdpk.device.exception.DeviceDeletionException;
import com.rdpk.device.exception.DeviceNotFoundException;
import com.rdpk.device.exception.DeviceUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Clock clock;
    
    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }
    
    @ExceptionHandler(DeviceUpdateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDeviceUpdateException(DeviceUpdateException e) {
        log.error("Device update failed: {}", e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("UPDATE_ERROR", e.getMessage(), LocalDateTime.now(clock))));
    }
    
    @ExceptionHandler(DeviceDeletionException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDeviceDeletionException(DeviceDeletionException e) {
        log.error("Device deletion failed: {}", e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("DELETION_ERROR", e.getMessage(), LocalDateTime.now(clock))));
    }
    
    @ExceptionHandler(DeviceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDeviceNotFoundException(DeviceNotFoundException e) {
        log.error("Device not found: {}", e.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", e.getMessage(), LocalDateTime.now(clock))));
    }
    
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.error("Validation failed: {}", message);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", message, LocalDateTime.now(clock))));
    }
    
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRuntimeException(RuntimeException e) {
        log.error("Unexpected runtime error: {}", e.getMessage(), e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage(), LocalDateTime.now(clock))));
    }
}


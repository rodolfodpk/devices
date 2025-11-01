package com.rdpk.config;

import com.rdpk.device.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    
    private Clock clock;
    private GlobalExceptionHandler handler;
    
    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC);
        handler = new GlobalExceptionHandler(clock);
    }
    
    @Test
    @DisplayName("Should handle unexpected RuntimeException and return 500")
    void shouldHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error occurred");
        
        // When
        var result = handler.handleRuntimeException(exception);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    ErrorResponse body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.error()).isEqualTo("INTERNAL_SERVER_ERROR");
                    assertThat(body.message()).isEqualTo("Unexpected error occurred");
                    assertThat(body.timestamp()).isEqualTo(LocalDateTime.now(clock));
                })
                .verifyComplete();
    }
}


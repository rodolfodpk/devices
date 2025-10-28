package com.rdpk.device.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    String error,
    String message,
    LocalDateTime timestamp
) {
    public ErrorResponse(String error, String message) {
        this(error, message, LocalDateTime.now());
    }
}


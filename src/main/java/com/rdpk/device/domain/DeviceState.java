package com.rdpk.device.domain;

import java.util.Optional;

public enum DeviceState {
    AVAILABLE,
    IN_USE,
    INACTIVE;
    
    /**
     * Parses a string to DeviceState enum value (case-insensitive).
     * 
     * @param value String value to parse (e.g., "available", "IN_USE", "inactive")
     * @return Optional with DeviceState if valid, empty if invalid or null
     */
    public static Optional<DeviceState> fromString(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}


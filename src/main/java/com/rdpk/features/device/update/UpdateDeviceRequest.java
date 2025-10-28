package com.rdpk.features.device.update;

import jakarta.validation.constraints.Size;

public record UpdateDeviceRequest(
    @Size(max = 100, message = "Device name must not exceed 100 characters")
    String name,
    
    @Size(max = 50, message = "Device brand must not exceed 50 characters")
    String brand,
    
    String state
) {}


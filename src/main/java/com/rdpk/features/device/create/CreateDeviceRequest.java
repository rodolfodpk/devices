package com.rdpk.features.device.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeviceRequest(
    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Device name must not exceed 100 characters")
    String name,
    
    @NotBlank(message = "Device brand is required")
    @Size(max = 50, message = "Device brand must not exceed 50 characters")
    String brand
) {}


package com.rdpk.device.dto;

import com.rdpk.device.domain.Device;
import java.time.LocalDateTime;

public record GetDeviceResponse(
    Long id,
    String name,
    String brand,
    String state,
    LocalDateTime createdAt
) {
    public static GetDeviceResponse from(Device device) {
        return new GetDeviceResponse(
            device.id(),
            device.name(),
            device.brand(),
            device.state().name(),
            device.createdAt()
        );
    }
}


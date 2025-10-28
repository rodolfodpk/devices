package com.rdpk.device.dto;

import com.rdpk.device.domain.Device;
import java.time.LocalDateTime;

public record CreateDeviceResponse(
    Long id,
    String name,
    String brand,
    String state,
    LocalDateTime createdAt
) {
    public static CreateDeviceResponse from(Device device) {
        return new CreateDeviceResponse(
            device.id(),
            device.name(),
            device.brand(),
            device.state().name(),
            device.createdAt()
        );
    }
}


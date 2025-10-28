package com.rdpk.features.device.update;

import com.rdpk.features.device.domain.Device;
import java.time.LocalDateTime;

public record UpdateDeviceResponse(
    Long id,
    String name,
    String brand,
    String state,
    LocalDateTime createdAt
) {
    public static UpdateDeviceResponse from(Device device) {
        return new UpdateDeviceResponse(
            device.id(),
            device.name(),
            device.brand(),
            device.state().name(),
            device.createdAt()
        );
    }
}


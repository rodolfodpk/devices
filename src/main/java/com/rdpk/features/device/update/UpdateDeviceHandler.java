package com.rdpk.features.device.update;

import com.rdpk.features.device.domain.Device;
import com.rdpk.features.device.domain.DeviceState;
import com.rdpk.features.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateDeviceHandler {
    
    private final DeviceRepository deviceRepository;
    
    public UpdateDeviceHandler(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public Mono<Device> updateDevice(Long id, UpdateDeviceRequest request) {
        return deviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Device not found")))
                .flatMap(device -> {
                    if (device.isInUse() && (request.name() != null || request.brand() != null)) {
                        return Mono.error(new DeviceUpdateException(
                            "Cannot update name or brand of device in use"
                        ));
                    }
                    return performUpdate(device, request);
                });
    }
    
    private Mono<Device> performUpdate(Device device, UpdateDeviceRequest request) {
        Device updated = device;
        
        if (request.name() != null) {
            updated = updated.withName(request.name());
        }
        
        if (request.brand() != null) {
            updated = updated.withBrand(request.brand());
        }
        
        if (request.state() != null) {
            try {
                DeviceState newState = DeviceState.valueOf(request.state().toUpperCase());
                updated = updated.withState(newState);
            } catch (IllegalArgumentException e) {
                return Mono.error(new DeviceUpdateException("Invalid state: " + request.state()));
            }
        }
        
        return deviceRepository.save(updated);
    }
}


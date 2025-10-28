package com.rdpk.features.device.delete;

import com.rdpk.features.device.domain.Device;
import com.rdpk.features.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeleteDeviceHandler {
    
    private final DeviceRepository deviceRepository;
    
    public DeleteDeviceHandler(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public Mono<Void> deleteDevice(Long id) {
        return deviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Device not found")))
                .flatMap(device -> {
                    if (device.isInUse()) {
                        return Mono.error(new DeviceDeletionException(
                            "Cannot delete device in use"
                        ));
                    }
                    return deviceRepository.deleteById(id);
                });
    }
}


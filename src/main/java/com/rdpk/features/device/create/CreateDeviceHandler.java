package com.rdpk.features.device.create;

import com.rdpk.features.device.domain.Device;
import com.rdpk.features.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateDeviceHandler {
    
    private final DeviceRepository deviceRepository;
    
    public CreateDeviceHandler(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public Mono<Device> createDevice(String name, String brand) {
        Device newDevice = new Device(name, brand);
        return deviceRepository.save(newDevice);
    }
}


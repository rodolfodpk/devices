package com.rdpk.features.device.fetch;

import com.rdpk.features.device.domain.Device;
import com.rdpk.features.device.domain.DeviceState;
import com.rdpk.features.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GetDeviceHandler {
    
    private final DeviceRepository deviceRepository;
    
    public GetDeviceHandler(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public Mono<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Device not found")));
    }
    
    public Flux<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
    
    public Flux<Device> getDevicesByBrand(String brand) {
        return deviceRepository.findByBrand(brand);
    }
    
    public Flux<Device> getDevicesByState(String state) {
        DeviceState deviceState;
        try {
            deviceState = DeviceState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Flux.error(new RuntimeException("Invalid state: " + state));
        }
        return deviceRepository.findByState(deviceState);
    }
}


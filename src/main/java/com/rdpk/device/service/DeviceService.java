package com.rdpk.device.service;

import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import com.rdpk.device.dto.UpdateDeviceRequest;
import com.rdpk.device.exception.DeviceDeletionException;
import com.rdpk.device.exception.DeviceNotFoundException;
import com.rdpk.device.exception.DeviceUpdateException;
import com.rdpk.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public Mono<Device> createDevice(String name, String brand) {
        Device newDevice = new Device(name, brand);
        return deviceRepository.save(newDevice);
    }
    
    public Mono<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("Device not found")));
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
    
    public Mono<Device> updateDevice(Long id, UpdateDeviceRequest request) {
        return deviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("Device not found")))
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
    
    public Mono<Void> deleteDevice(Long id) {
        return deviceRepository.findById(id)
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("Device not found")))
                .flatMap(device -> {
                    if (!device.isDeletable()) {
                        return Mono.error(new DeviceDeletionException(
                            "Cannot delete device that is in use or inactive"
                        ));
                    }
                    return deviceRepository.deleteById(id);
                });
    }
}


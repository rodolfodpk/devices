package com.rdpk.device.repository;

import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceRepository {
    Mono<Device> save(Device device);
    Mono<Device> findById(Long id);
    Mono<Void> deleteById(Long id);
    Flux<Device> findAll();
    Flux<Device> findByBrand(String brand);
    Flux<Device> findByState(DeviceState state);
    Mono<Boolean> existsById(Long id);
}


package com.rdpk.device.repository;

import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceRepository extends ReactiveCrudRepository<Device, Long> {
    
    // Pagination support
    Flux<Device> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Flux<Device> findByBrandOrderByCreatedAtDesc(String brand, Pageable pageable);
    Mono<Long> countByBrand(String brand);
    
    Flux<Device> findByStateOrderByCreatedAtDesc(DeviceState state, Pageable pageable);
    Mono<Long> countByState(DeviceState state);
    
    // Non-paginated methods (backward compatibility)
    Flux<Device> findAllByOrderByCreatedAtDesc();
    Flux<Device> findByBrandOrderByCreatedAtDesc(String brand);
    Flux<Device> findByStateOrderByCreatedAtDesc(DeviceState state);
}


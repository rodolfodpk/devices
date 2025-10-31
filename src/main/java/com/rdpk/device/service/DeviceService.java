package com.rdpk.device.service;

import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import com.rdpk.device.exception.DeviceDeletionException;
import com.rdpk.device.exception.DeviceNotFoundException;
import com.rdpk.device.exception.DeviceUpdateException;
import com.rdpk.device.repository.DeviceRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final Clock clock;
    
    public DeviceService(
            DeviceRepository deviceRepository,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry,
            Clock clock) {
        this.deviceRepository = deviceRepository;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.timeLimiterRegistry = timeLimiterRegistry;
        this.clock = clock;
    }
    
    private <T> Mono<T> applyResilience(Mono<T> mono) {
        return mono
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("devices")))
                .transformDeferred(RetryOperator.of(retryRegistry.retry("devices")))
                .transformDeferred(TimeLimiterOperator.of(timeLimiterRegistry.timeLimiter("devices")));
    }
    
    private <T> Flux<T> applyResilience(Flux<T> flux) {
        return flux
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("devices")))
                .transformDeferred(RetryOperator.of(retryRegistry.retry("devices")))
                .transformDeferred(TimeLimiterOperator.of(timeLimiterRegistry.timeLimiter("devices")));
    }
    
    public Mono<Device> createDevice(String name, String brand) {
        LocalDateTime now = LocalDateTime.now(clock);
        Device newDevice = new Device(null, name, brand, DeviceState.AVAILABLE, now);
        return applyResilience(deviceRepository.save(newDevice));
    }
    
    public Mono<Device> getDeviceById(Long id) {
        return applyResilience(deviceRepository.findById(id))
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("Device not found")));
    }
    
    // Pagination methods
    public Flux<Device> getAllDevices(Pageable pageable) {
        return applyResilience(deviceRepository.findAllByOrderByCreatedAtDesc(pageable));
    }
    
    public Mono<Long> countAllDevices() {
        return applyResilience(deviceRepository.count());
    }
    
    public Flux<Device> getDevicesByBrand(String brand, Pageable pageable) {
        return applyResilience(deviceRepository.findByBrand(brand, pageable));
    }
    
    public Mono<Long> countByBrand(String brand) {
        return applyResilience(deviceRepository.countByBrand(brand));
    }
    
    public Flux<Device> getDevicesByState(DeviceState state, Pageable pageable) {
        return applyResilience(deviceRepository.findByState(state, pageable));
    }
    
    public Mono<Long> countByState(DeviceState state) {
        return applyResilience(deviceRepository.countByState(state));
    }
    
    // Non-paginated methods (backward compatibility)
    public Flux<Device> getAllDevices() {
        return applyResilience(deviceRepository.findAllByOrderByCreatedAtDesc());
    }
    
    public Flux<Device> getDevicesByBrand(String brand) {
        return applyResilience(deviceRepository.findByBrandOrderByCreatedAtDesc(brand));
    }
    
    public Flux<Device> getDevicesByState(String state) {
        return DeviceState.fromString(state)
                .map(deviceState -> applyResilience(deviceRepository.findByStateOrderByCreatedAtDesc(deviceState)))
                .orElse(Flux.error(new RuntimeException("Invalid state: " + state)));
    }
    
    /**
     * Partially updates a device.
     * 
     * <p>Implements partial update semantics: only non-null parameters are updated.
     * Null parameters are ignored, leaving the corresponding database fields unchanged.
     * 
     * <p>Domain validation:
     * <ul>
     *   <li>Cannot update name or brand of a device that is IN_USE</li>
     *   <li>State can always be updated regardless of current state</li>
     * </ul>
     * 
     * @param id Device ID to update
     * @param name New name (null = don't update)
     * @param brand New brand (null = don't update)
     * @param state New state (null = don't update)
     * @return Updated device
     * @throws DeviceNotFoundException if device not found
     * @throws DeviceUpdateException if attempting to update name/brand of device in use
     */
    public Mono<Device> updateDevice(Long id, String name, String brand, DeviceState state) {
        return applyResilience(deviceRepository.findById(id))
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("Device not found")))
                .flatMap(device -> {
                    if (device.isInUse() && (name != null || brand != null)) {
                        return Mono.error(new DeviceUpdateException(
                            "Cannot update name or brand of device in use"
                        ));
                    }
                    return performUpdate(device, name, brand, state);
                });
    }
    
    /**
     * Performs the partial update by applying only non-null fields.
     * 
     * <p>This method implements the core partial update logic:
     * - Null parameters = field not updated (keeps existing value)
     * - Non-null parameters = field updated to new value
     * 
     * @param device Existing device from database
     * @param name New name value (null = keep existing)
     * @param brand New brand value (null = keep existing)
     * @param state New state value (null = keep existing)
     * @return Updated device
     */
    private Mono<Device> performUpdate(Device device, String name, String brand, DeviceState state) {
        Device updated = device;
        
        if (name != null) {
            updated = updated.withName(name);
        }
        
        if (brand != null) {
            updated = updated.withBrand(brand);
        }
        
        if (state != null) {
            updated = updated.withState(state);
        }
        
        return applyResilience(deviceRepository.save(updated));
    }
    
    public Mono<Void> deleteDevice(Long id) {
        return applyResilience(deviceRepository.findById(id))
                .switchIfEmpty(Mono.error(new DeviceNotFoundException("Device not found")))
                .flatMap(device -> {
                    if (!device.isDeletable()) {
                        return Mono.error(new DeviceDeletionException(
                            "Cannot delete device that is in use or inactive"
                        ));
                    }
                    return applyResilience(deviceRepository.deleteById(id));
                });
    }
}


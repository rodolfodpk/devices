package com.rdpk.device.controller;

import com.rdpk.device.domain.DeviceState;
import com.rdpk.device.dto.CreateDeviceRequest;
import com.rdpk.device.dto.CreateDeviceResponse;
import com.rdpk.device.dto.GetDeviceResponse;
import com.rdpk.device.dto.PagedResponse;
import com.rdpk.device.dto.UpdateDeviceRequest;
import com.rdpk.device.dto.UpdateDeviceResponse;
import com.rdpk.device.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    
    private final DeviceService deviceService;
    
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
    
    @PostMapping
    public Mono<ResponseEntity<CreateDeviceResponse>> createDevice(
            @Valid @RequestBody CreateDeviceRequest request) {
        return deviceService.createDevice(request.name(), request.brand())
                .map(CreateDeviceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
    
    /**
     * Gets all devices with optional filtering and pagination.
     * 
     * <p>Supports:
     * <ul>
     *   <li>Filtering by brand: {@code ?brand=Apple}</li>
     *   <li>Filtering by state: {@code ?state=AVAILABLE}</li>
     *   <li>Pagination: {@code ?page=0&size=20}</li>
     * </ul>
     * 
     * <p>Sorting: Results are always sorted by {@code createdAt DESC} (newest first).
     * Custom sorting is not currently supported due to Spring Data R2DBC limitations.
     * 
     * @param brand Optional brand filter
     * @param state Optional state filter (AVAILABLE, IN_USE, INACTIVE)
     * @param page Page number (0-indexed, optional, defaults to 0)
     * @param size Page size (optional, defaults to 20, max 100)
     * @return Paginated response if pagination params provided, otherwise list of all devices
     */
    @GetMapping
    public Mono<ResponseEntity<?>> getAllDevices(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // If pagination parameters are provided, use paginated endpoints
        if (page != null || size != null) {
            // Validate pagination parameters
            if (page != null && page < 0) {
                return Mono.just(ResponseEntity.badRequest().build());
            }
            if (size != null && (size < 1 || size > 100)) {
                return Mono.just(ResponseEntity.badRequest().build());
            }
            
            // Default sort: createdAt DESC
            // Note: Spring Data R2DBC doesn't override OrderBy with Sort from Pageable,
            // so we use the default sort defined in repository method name
            int pageNumber = page != null ? page : 0;
            int pageSize = size != null ? size : 20;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            
            if (brand != null) {
                return Mono.zip(
                        deviceService.getDevicesByBrand(brand, pageable)
                                .map(GetDeviceResponse::from)
                                .collectList(),
                        deviceService.countByBrand(brand)
                ).map(tuple -> ResponseEntity.ok(
                        PagedResponse.of(tuple.getT1(), pageNumber, pageSize, tuple.getT2()))
                );
            }
            
            if (state != null) {
                DeviceState deviceState;
                try {
                    deviceState = DeviceState.valueOf(state.toUpperCase());
                    return Mono.zip(
                            deviceService.getDevicesByState(deviceState, pageable)
                                    .map(GetDeviceResponse::from)
                                    .collectList(),
                            deviceService.countByState(deviceState)
                    ).map(tuple -> ResponseEntity.ok(
                            PagedResponse.of(tuple.getT1(), pageNumber, pageSize, tuple.getT2()))
                    );
                } catch (IllegalArgumentException e) {
                    return Mono.just(ResponseEntity.badRequest().build());
                }
            }
            
            // Paginated all devices
            return Mono.zip(
                    deviceService.getAllDevices(pageable)
                            .map(GetDeviceResponse::from)
                            .collectList(),
                    deviceService.countAllDevices()
            ).map(tuple -> ResponseEntity.ok(
                    PagedResponse.of(tuple.getT1(), pageNumber, pageSize, tuple.getT2()))
            );
        }
        
        // Non-paginated (backward compatibility)
        if (brand != null) {
            return deviceService.getDevicesByBrand(brand)
                    .map(GetDeviceResponse::from)
                    .collectList()
                    .map(ResponseEntity::ok);
        }
        
        if (state != null) {
            return deviceService.getDevicesByState(state)
                    .map(GetDeviceResponse::from)
                    .collectList()
                    .map(ResponseEntity::ok);
        }
        
        return deviceService.getAllDevices()
                .map(GetDeviceResponse::from)
                .collectList()
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<GetDeviceResponse>> getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id)
                .map(GetDeviceResponse::from)
                .map(ResponseEntity::ok);
    }
    
    /**
     * Partially updates a device.
     * 
     * <p>This is a PATCH operation that supports partial updates. Only fields provided 
     * (non-null) in the request will be updated. Fields that are null or not provided 
     * will remain unchanged in the database.
     * 
     * <p>Example: If you send {"state": "IN_USE"}, only the state will be updated,
     * while name and brand remain unchanged.
     * 
     * <p>Domain rules apply:
     * <ul>
     *   <li>Cannot update name or brand of a device that is IN_USE</li>
     *   <li>State can always be updated regardless of current state</li>
     * </ul>
     * 
     * @param id The device ID to update
     * @param request Partial update request - only non-null fields will be updated
     * @return Updated device response
     */
    @PatchMapping("/{id}")
    public Mono<ResponseEntity<UpdateDeviceResponse>> updateDevice(
            @PathVariable Long id,
            @RequestBody UpdateDeviceRequest request) {
        DeviceState state = null;
        if (request.state() != null) {
            try {
                state = DeviceState.valueOf(request.state().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Mono.just(ResponseEntity.badRequest().build());
            }
        }
        return deviceService.updateDevice(id, request.name(), request.brand(), state)
                .map(UpdateDeviceResponse::from)
                .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDevice(@PathVariable Long id) {
        return deviceService.deleteDevice(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}


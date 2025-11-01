package com.rdpk.device.controller;

import com.rdpk.device.domain.Device;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

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
     * <p>Pagination: If not provided, defaults to {@code page=0} and {@code size=20}.
     * All responses are paginated for consistency and safety.
     * 
     * <p>Sorting: Results are always sorted by {@code createdAt DESC} (newest first).
     * Custom sorting is not currently supported due to Spring Data R2DBC limitations.
     * 
     * @param brand Optional brand filter
     * @param state Optional state filter (AVAILABLE, IN_USE, INACTIVE)
     * @param page Page number (0-indexed, optional, defaults to 0)
     * @param size Page size (optional, defaults to 20, max 100)
     * @return Paginated response with devices
     */
    @GetMapping
    public Mono<ResponseEntity<?>> getAllDevices(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // Validate pagination parameters if provided
        if (page != null || size != null) {
            if (!isValidPagination(page, size)) {
                return Mono.just(ResponseEntity.badRequest().build());
            }
        }
        
        // Default pagination: page=0, size=20
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        
        if (brand != null) {
            return buildPaginatedResponse(
                    deviceService.getDevicesByBrand(brand, pageable),
                    deviceService.countByBrand(brand),
                    pageNumber, pageSize
            );
        }
        
        if (state != null) {
            return DeviceState.fromString(state)
                    .map(deviceState -> buildPaginatedResponse(
                            deviceService.getDevicesByState(deviceState, pageable),
                            deviceService.countByState(deviceState),
                            pageNumber, pageSize
                    ))
                    .orElse(Mono.just(ResponseEntity.badRequest().build()));
        }
        
        // Paginated all devices
        return buildPaginatedResponse(
                deviceService.getAllDevices(pageable),
                deviceService.countAllDevices(),
                pageNumber, pageSize
        );
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
        if (request.state() != null) {
            Optional<DeviceState> parsedState = DeviceState.fromString(request.state());
            if (parsedState.isEmpty()) {
                return Mono.just(ResponseEntity.badRequest().build());
            }
            return deviceService.updateDevice(id, request.name(), request.brand(), parsedState.get())
                    .map(UpdateDeviceResponse::from)
                    .map(ResponseEntity::ok);
        }
        return deviceService.updateDevice(id, request.name(), request.brand(), null)
                .map(UpdateDeviceResponse::from)
                .map(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDevice(@PathVariable Long id) {
        return deviceService.deleteDevice(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
    
    /**
     * Validates pagination parameters.
     * 
     * @param page Page number (must be >= 0 if provided)
     * @param size Page size (must be between 1 and 100 if provided)
     * @return true if valid, false otherwise
     */
    private boolean isValidPagination(Integer page, Integer size) {
        return (page == null || page >= 0) && (size == null || (size >= 1 && size <= 100));
    }
    
    
    /**
     * Builds a paginated response from devices flux and count mono.
     * 
     * @param devices Flux of devices to paginate
     * @param count Mono of total count
     * @param pageNumber Current page number
     * @param pageSize Page size
     * @return Mono of ResponseEntity with PagedResponse
     */
    private Mono<ResponseEntity<?>> buildPaginatedResponse(
            Flux<Device> devices, Mono<Long> count, int pageNumber, int pageSize) {
        return Mono.zip(
                devices.map(GetDeviceResponse::from).collectList(),
                count
        ).map(tuple -> ResponseEntity.ok(
                PagedResponse.of(tuple.getT1(), pageNumber, pageSize, tuple.getT2())
        ));
    }
}


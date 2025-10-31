package com.rdpk.device.controller;

import com.rdpk.device.domain.DeviceState;
import com.rdpk.device.dto.CreateDeviceRequest;
import com.rdpk.device.dto.CreateDeviceResponse;
import com.rdpk.device.dto.GetDeviceResponse;
import com.rdpk.device.dto.UpdateDeviceRequest;
import com.rdpk.device.dto.UpdateDeviceResponse;
import com.rdpk.device.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

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
    
    @GetMapping
    public Mono<ResponseEntity<List<GetDeviceResponse>>> getAllDevices(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String state) {
        
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
    
    @PutMapping("/{id}")
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


package com.rdpk.features.device.create;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/devices")
public class CreateDeviceController {
    
    private final CreateDeviceHandler createDeviceHandler;
    
    public CreateDeviceController(CreateDeviceHandler createDeviceHandler) {
        this.createDeviceHandler = createDeviceHandler;
    }
    
    @PostMapping
    public Mono<ResponseEntity<CreateDeviceResponse>> createDevice(
            @Valid @RequestBody CreateDeviceRequest request) {
        return createDeviceHandler.createDevice(request.name(), request.brand())
                .map(CreateDeviceResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
}


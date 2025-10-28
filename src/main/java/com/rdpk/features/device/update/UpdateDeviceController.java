package com.rdpk.features.device.update;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/devices")
public class UpdateDeviceController {
    
    private final UpdateDeviceHandler updateDeviceHandler;
    
    public UpdateDeviceController(UpdateDeviceHandler updateDeviceHandler) {
        this.updateDeviceHandler = updateDeviceHandler;
    }
    
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UpdateDeviceResponse>> updateDevice(
            @PathVariable Long id,
            @RequestBody UpdateDeviceRequest request) {
        return updateDeviceHandler.updateDevice(id, request)
                .map(UpdateDeviceResponse::from)
                .map(ResponseEntity::ok)
                .onErrorResume(DeviceUpdateException.class, e ->
                    Mono.just(ResponseEntity.badRequest()
                            .body(new UpdateDeviceResponse(null, null, null, null, null)))
                )
                .onErrorResume(RuntimeException.class, e ->
                    Mono.just(ResponseEntity.notFound().build())
                );
    }
}


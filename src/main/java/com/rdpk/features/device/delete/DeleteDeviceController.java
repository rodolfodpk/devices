package com.rdpk.features.device.delete;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/devices")
public class DeleteDeviceController {
    
    private final DeleteDeviceHandler deleteDeviceHandler;
    
    public DeleteDeviceController(DeleteDeviceHandler deleteDeviceHandler) {
        this.deleteDeviceHandler = deleteDeviceHandler;
    }
    
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDevice(@PathVariable Long id) {
        return deleteDeviceHandler.deleteDevice(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(DeviceDeletionException.class, e ->
                    Mono.just(ResponseEntity.badRequest().build())
                )
                .onErrorResume(RuntimeException.class, e ->
                    Mono.just(ResponseEntity.notFound().build())
                );
    }
}


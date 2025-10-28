package com.rdpk.features.device.fetch;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class GetDeviceController {
    
    private final GetDeviceHandler getDeviceHandler;
    
    public GetDeviceController(GetDeviceHandler getDeviceHandler) {
        this.getDeviceHandler = getDeviceHandler;
    }
    
    @GetMapping
    public Mono<ResponseEntity<List<GetDeviceResponse>>> getAllDevices(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String state) {
        
        if (brand != null) {
            return getDeviceHandler.getDevicesByBrand(brand)
                    .map(GetDeviceResponse::from)
                    .collectList()
                    .map(ResponseEntity::ok);
        }
        
        if (state != null) {
            return getDeviceHandler.getDevicesByState(state)
                    .map(GetDeviceResponse::from)
                    .collectList()
                    .map(ResponseEntity::ok);
        }
        
        return getDeviceHandler.getAllDevices()
                .map(GetDeviceResponse::from)
                .collectList()
                .map(ResponseEntity::ok);
    }
    
    @GetMapping("/{id}")
    public Mono<ResponseEntity<GetDeviceResponse>> getDeviceById(@PathVariable Long id) {
        return getDeviceHandler.getDeviceById(id)
                .map(GetDeviceResponse::from)
                .map(ResponseEntity::ok)
                .onErrorResume(RuntimeException.class, e ->
                    Mono.just(ResponseEntity.notFound().build())
                );
    }
}


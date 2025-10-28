package com.rdpk.device.integration.controller;

import com.rdpk.device.AbstractIntegrationTest;
import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import com.rdpk.device.fixture.DeviceFixture;
import com.rdpk.device.repository.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

@DisplayName("DeviceController Integration Tests")
class DeviceControllerIntegrationTest extends AbstractIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    // databaseClient and webTestClient are inherited from AbstractIntegrationTest
    
    @Test
    @DisplayName("POST /api/v1/devices - Should create a new device")
    void shouldCreateDevice() {
        // Given
        String requestBody = """
                {
                    "name": "iPhone 15",
                    "brand": "Apple"
                }
                """;
        
        // When & Then
        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("iPhone 15")
                .jsonPath("$.brand").isEqualTo("Apple")
                .jsonPath("$.state").isEqualTo("AVAILABLE")
                .jsonPath("$.id").exists()
                .jsonPath("$.createdAt").exists();
    }
    
    @Test
    @DisplayName("POST /api/v1/devices - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() {
        // Given
        String requestBody = """
                {
                    "name": "",
                    "brand": "Apple"
                }
                """;
        
        // When & Then
        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }
    
    @Test
    @DisplayName("GET /api/v1/devices - Should return all devices")
    void shouldGetAllDevices() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 1", "Brand")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 2", "Brand")).block();
        
        // When & Then
        webTestClient.get()
                .uri("/api/v1/devices")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Device.class)
                .hasSize(2);
    }
    
    @Test
    @DisplayName("GET /api/v1/devices?brand=Apple - Should filter by brand")
    void shouldFilterByBrand() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice("iPhone", "Apple")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("Galaxy", "Samsung")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("iPad", "Apple")).block();
        
        // When & Then
        webTestClient.get()
                .uri("/api/v1/devices?brand=Apple")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].brand").isEqualTo("Apple")
                .jsonPath("$[1].brand").isEqualTo("Apple");
    }
    
    @Test
    @DisplayName("GET /api/v1/devices?state=AVAILABLE - Should filter by state")
    void shouldFilterByState() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice()).block();
        Device inUse2 = DeviceFixture.createDeviceWithState("Device 2", "Brand", DeviceState.IN_USE);
        deviceRepository.save(inUse2).block();
        Device inUse3 = DeviceFixture.createDeviceWithState("Device 3", "Brand", DeviceState.IN_USE);
        deviceRepository.save(inUse3).block();
        
        // When & Then
        webTestClient.get()
                .uri("/api/v1/devices?state=AVAILABLE")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].state").isEqualTo("AVAILABLE");
    }
    
    @Test
    @DisplayName("GET /api/v1/devices/{id} - Should return device by ID")
    void shouldGetDeviceById() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // When & Then
        webTestClient.get()
                .uri("/api/v1/devices/{id}", saved.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(saved.id())
                .jsonPath("$.name").isEqualTo(saved.name())
                .jsonPath("$.brand").isEqualTo(saved.brand());
    }
    
    @Test
    @DisplayName("GET /api/v1/devices/{id} - Should return 404 when device not found")
    void shouldReturn404WhenDeviceNotFound() {
        // When & Then
        webTestClient.get()
                .uri("/api/v1/devices/999")
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    @DisplayName("PUT /api/v1/devices/{id} - Should update device successfully")
    void shouldUpdateDevice() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        String requestBody = """
                {
                    "name": "Updated Name",
                    "brand": "Updated Brand",
                    "state": "IN_USE"
                }
                """;
        
        // When & Then
        webTestClient.put()
                .uri("/api/v1/devices/{id}", saved.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated Name")
                .jsonPath("$.brand").isEqualTo("Updated Brand")
                .jsonPath("$.state").isEqualTo("IN_USE");
    }
    
    @Test
    @DisplayName("PUT /api/v1/devices/{id} - Should return 400 when updating name of device in use")
    void shouldReturn400WhenUpdatingInUseDevice() {
        // Given
        Device inUse = deviceRepository.save(DeviceFixture.createDeviceWithState("Device", "Brand", DeviceState.IN_USE))
                .block();
        String requestBody = """
                {
                    "name": "New Name"
                }
                """;
        
        // When & Then
        webTestClient.put()
                .uri("/api/v1/devices/{id}", inUse.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }
    
    @Test
    @DisplayName("PUT /api/v1/devices/{id} - Should return 404 when device not found")
    void shouldReturn404WhenUpdatingNonExistentDevice() {
        // Given
        String requestBody = """
                {
                    "name": "New Name"
                }
                """;
        
        // When & Then
        webTestClient.put()
                .uri("/api/v1/devices/999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    @DisplayName("DELETE /api/v1/devices/{id} - Should delete device successfully")
    void shouldDeleteDevice() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // When & Then
        webTestClient.delete()
                .uri("/api/v1/devices/{id}", saved.id())
                .exchange()
                .expectStatus().isNoContent();
        
        // Verify deletion
        deviceRepository.findById(saved.id())
                .as(StepVerifier::create)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("DELETE /api/v1/devices/{id} - Should return 400 when deleting device in use")
    void shouldReturn400WhenDeletingInUseDevice() {
        // Given
        Device inUse = deviceRepository.save(DeviceFixture.createDeviceWithState("Device", "Brand", DeviceState.IN_USE))
                .block();
        
        // When & Then
        webTestClient.delete()
                .uri("/api/v1/devices/{id}", inUse.id())
                .exchange()
                .expectStatus().isBadRequest();
    }
    
    @Test
    @DisplayName("DELETE /api/v1/devices/{id} - Should return 404 when device not found")
    void shouldReturn404WhenDeletingNonExistentDevice() {
        // When & Then
        webTestClient.delete()
                .uri("/api/v1/devices/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}


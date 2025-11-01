package com.rdpk.device.e2e;

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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Device E2E Tests")
class DeviceE2ETest extends AbstractIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    // databaseClient and webTestClient are inherited from AbstractIntegrationTest
    
    @Test
    @DisplayName("Complete device lifecycle: create → use → update → release → delete")
    void shouldHandleCompleteDeviceLifecycle() {
        // 1. Create device
        String createRequest = """
                {
                    "name": "iPhone 15 Pro",
                    "brand": "Apple"
                }
                """;
        
        // Create device
        webTestClient.post()
                .uri("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.state").isEqualTo("AVAILABLE");
        
        // Get device ID from database
        Device created = deviceRepository.findAll()
                .collectList()
                .block()
                .get(0);
        Long deviceId = created.id();
        
        assertThat(deviceId).isNotNull();
        
        // 2. Get all devices - should see our device (now paginated)
        webTestClient.get()
                .uri("/api/v1/devices")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo(deviceId);
        
        // 3. Set device to IN_USE
        String updateToInUse = """
                {
                    "state": "IN_USE"
                }
                """;
        
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateToInUse)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.state").isEqualTo("IN_USE");
        
        // 4. Verify domain validation: cannot update name/brand while in use
        String updateName = """
                {
                    "name": "Updated Name"
                }
                """;
        
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateName)
                .exchange()
                .expectStatus().isBadRequest();
        
        // 5. Set device back to AVAILABLE
        String updateToAvailable = """
                {
                    "state": "AVAILABLE"
                }
                """;
        
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateToAvailable)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.state").isEqualTo("AVAILABLE");
        
        // 6. Now can update name/brand
        String updateNameAndBrand = """
                {
                    "name": "iPhone 15 Pro Max",
                    "brand": "Apple"
                }
                """;
        
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateNameAndBrand)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("iPhone 15 Pro Max");
        
        // 7. Verify deletion protection: try to delete IN_USE device
        // First set to IN_USE
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"state\": \"IN_USE\"}")
                .exchange();
        
        // Try to delete
        webTestClient.delete()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isBadRequest();
        
        // 8. Set back to AVAILABLE and delete
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"state\": \"AVAILABLE\"}")
                .exchange();
        
        webTestClient.delete()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isNoContent();
        
        // 9. Verify device is deleted
        webTestClient.get()
                .uri("/api/v1/devices/{id}", deviceId)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    @DisplayName("Should handle multiple devices concurrently")
    void shouldHandleMultipleDevicesConcurrently() {
        // Create multiple devices
        for (int i = 0; i < 5; i++) {
            String request = String.format("""
                    {
                        "name": "Device %d",
                        "brand": "Brand %d"
                    }
                    """, i, i);
            
            webTestClient.post()
                    .uri("/api/v1/devices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();
        }
        
        // Verify all are created (now paginated)
        webTestClient.get()
                .uri("/api/v1/devices")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(5)
                .jsonPath("$.totalElements").isEqualTo(5);
    }
    
    @Test
    @DisplayName("Should handle state transitions correctly")
    void shouldHandleStateTransitions() {
        // Create device
        Device created = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // Transition: AVAILABLE → IN_USE
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"state\": \"IN_USE\"}")
                .exchange()
                .expectStatus().isOk();
        
        Device inUse = deviceRepository.findById(created.id()).block();
        assertThat(inUse.state()).isEqualTo(DeviceState.IN_USE);
        
        // Transition: IN_USE → AVAILABLE
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"state\": \"AVAILABLE\"}")
                .exchange()
                .expectStatus().isOk();
        
        Device available = deviceRepository.findById(created.id()).block();
        assertThat(available.state()).isEqualTo(DeviceState.AVAILABLE);
    }
    
    @Test
    @DisplayName("Should maintain createdAt timestamp immutability")
    void shouldMaintainCreatedAtImmutability() {
        // Create device
        Device created = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // Try to update (this doesn't change createdAt, but we verify it stays)
        webTestClient.patch()
                .uri("/api/v1/devices/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"Updated\"}")
                .exchange();
        
        // Get updated device
        Device updated = deviceRepository.findById(created.id()).block();
        
        // Verify createdAt is unchanged (domain validation)
        // Note: Truncate to milliseconds to account for microsecond-level database precision variations
        assertThat(updated.createdAt().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
                .isEqualTo(created.createdAt().truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        assertThat(updated.name()).isEqualTo("Updated"); // Name was updated
    }
}


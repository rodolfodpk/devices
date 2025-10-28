package com.rdpk.device.integration.service;

import com.rdpk.device.AbstractIntegrationTest;
import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import com.rdpk.device.dto.UpdateDeviceRequest;
import com.rdpk.device.exception.DeviceDeletionException;
import com.rdpk.device.exception.DeviceUpdateException;
import com.rdpk.device.fixture.DeviceFixture;
import com.rdpk.device.repository.DeviceRepository;
import com.rdpk.device.service.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeviceService Integration Tests")
class DeviceServiceIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private DatabaseClient databaseClient;
    
    @BeforeEach
    void setUp() {
        // Database cleanup - Spring Test manages context, container starts before tests
        databaseClient.sql("DELETE FROM devices")
                .fetch()
                .rowsUpdated()
                .block();
    }
    
    @Test
    @DisplayName("Should create a new device")
    void shouldCreateDevice() {
        // When
        StepVerifier.create(deviceService.createDevice("Test Device", "Test Brand"))
                .assertNext(device -> {
                    assertThat(device.id()).isNotNull();
                    assertThat(device.name()).isEqualTo("Test Device");
                    assertThat(device.brand()).isEqualTo("Test Brand");
                    assertThat(device.state()).isEqualTo(DeviceState.AVAILABLE);
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should get device by ID")
    void shouldGetDeviceById() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // When
        StepVerifier.create(deviceService.getDeviceById(saved.id()))
                .assertNext(device -> {
                    assertThat(device.id()).isEqualTo(saved.id());
                    assertThat(device.name()).isEqualTo(saved.name());
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should throw exception when device not found")
    void shouldThrowExceptionWhenDeviceNotFound() {
        // When
        StepVerifier.create(deviceService.getDeviceById(999L))
                .expectErrorMatches(e -> e instanceof RuntimeException && 
                    e.getMessage().equals("Device not found"))
                .verify();
    }
    
    @Test
    @DisplayName("Should get all devices")
    void shouldGetAllDevices() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 1", "Brand")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 2", "Brand")).block();
        
        // When
        Long count = deviceService.getAllDevices()
                .count()
                .block();
        
        // Then
        assertThat(count).isEqualTo(2L);
    }
    
    @Test
    @DisplayName("Should get devices by brand")
    void shouldGetDevicesByBrand() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice("iPhone", "Apple")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("Samsung", "Samsung")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("iPad", "Apple")).block();
        
        // When
        Long count = deviceService.getDevicesByBrand("Apple")
                .count()
                .block();
        
        // Then
        assertThat(count).isEqualTo(2L);
    }
    
    @Test
    @DisplayName("Should get devices by state")
    void shouldGetDevicesByState() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice()).block();
        Device inUse2 = DeviceFixture.createDeviceWithState("Device 2", "Brand", DeviceState.IN_USE);
        deviceRepository.save(inUse2).block();
        Device inUse3 = DeviceFixture.createDeviceWithState("Device 3", "Brand", DeviceState.IN_USE);
        deviceRepository.save(inUse3).block();
        
        // When
        Long count = deviceService.getDevicesByState("IN_USE")
                .count()
                .block();
        
        // Then
        assertThat(count).isEqualTo(2L);
    }
    
    @Test
    @DisplayName("Should return error for invalid state")
    void shouldReturnErrorForInvalidState() {
        // When
        StepVerifier.create(deviceService.getDevicesByState("INVALID_STATE"))
                .expectErrorMatches(e -> e instanceof RuntimeException && 
                    e.getMessage().contains("Invalid state"))
                .verify();
    }
    
    @Test
    @DisplayName("Should update device successfully when device is available")
    void shouldUpdateDeviceWhenAvailable() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        UpdateDeviceRequest request = new UpdateDeviceRequest("Updated Name", "Updated Brand", null);
        
        // When
        StepVerifier.create(deviceService.updateDevice(saved.id(), request))
                .assertNext(device -> {
                    assertThat(device.id()).isEqualTo(saved.id());
                    assertThat(device.name()).isEqualTo("Updated Name");
                    assertThat(device.brand()).isEqualTo("Updated Brand");
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should throw exception when updating name of device in use")
    void shouldThrowExceptionWhenUpdatingNameOfDeviceInUse() {
        // Given
        Device inUse = deviceRepository.save(DeviceFixture.createDeviceWithState("Device", "Brand", DeviceState.IN_USE))
                .block();
        UpdateDeviceRequest request = new UpdateDeviceRequest("New Name", null, null);
        
        // When
        StepVerifier.create(deviceService.updateDevice(inUse.id(), request))
                .expectError(DeviceUpdateException.class)
                .verify();
    }
    
    @Test
    @DisplayName("Should throw exception when updating brand of device in use")
    void shouldThrowExceptionWhenUpdatingBrandOfDeviceInUse() {
        // Given
        Device inUse = deviceRepository.save(DeviceFixture.createDeviceWithState("Device", "Brand", DeviceState.IN_USE))
                .block();
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, "New Brand", null);
        
        // When
        StepVerifier.create(deviceService.updateDevice(inUse.id(), request))
                .expectError(DeviceUpdateException.class)
                .verify();
    }
    
    @Test
    @DisplayName("Should allow state update for device in use")
    void shouldAllowStateUpdateForDeviceInUse() {
        // Given
        Device inUse = deviceRepository.save(DeviceFixture.createDeviceWithState("Device", "Brand", DeviceState.IN_USE))
                .block();
        UpdateDeviceRequest request = new UpdateDeviceRequest(null, null, "AVAILABLE");
        
        // When
        StepVerifier.create(deviceService.updateDevice(inUse.id(), request))
                .assertNext(device -> {
                    assertThat(device.state()).isEqualTo(DeviceState.AVAILABLE);
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should throw exception when update device not found")
    void shouldThrowExceptionWhenUpdateDeviceNotFound() {
        // Given
        UpdateDeviceRequest request = new UpdateDeviceRequest("Name", null, null);
        
        // When
        StepVerifier.create(deviceService.updateDevice(999L, request))
                .expectErrorMatches(e -> e instanceof RuntimeException && 
                    e.getMessage().equals("Device not found"))
                .verify();
    }
    
    @Test
    @DisplayName("Should delete device when it is available")
    void shouldDeleteDeviceWhenAvailable() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // When
        StepVerifier.create(deviceService.deleteDevice(saved.id()))
                .verifyComplete();
        
        // Then
        StepVerifier.create(deviceRepository.findById(saved.id()))
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should throw exception when deleting device in use")
    void shouldThrowExceptionWhenDeletingDeviceInUse() {
        // Given
        Device inUse = deviceRepository.save(DeviceFixture.createDeviceWithState("Device", "Brand", DeviceState.IN_USE))
                .block();
        
        // When
        StepVerifier.create(deviceService.deleteDevice(inUse.id()))
                .expectError(DeviceDeletionException.class)
                .verify();
        
        // Then device should still exist
        StepVerifier.create(deviceRepository.findById(inUse.id()))
                .assertNext(device -> assertThat(device).isNotNull())
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should throw exception when delete device not found")
    void shouldThrowExceptionWhenDeleteDeviceNotFound() {
        // When
        StepVerifier.create(deviceService.deleteDevice(999L))
                .expectErrorMatches(e -> e instanceof RuntimeException && 
                    e.getMessage().equals("Device not found"))
                .verify();
    }
}


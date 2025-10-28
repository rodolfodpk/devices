package com.rdpk.device.integration.repository;

import com.rdpk.device.AbstractIntegrationTest;
import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import com.rdpk.device.fixture.DeviceFixture;
import com.rdpk.device.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeviceRepository Integration Tests")
class DeviceRepositoryIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private DatabaseClient databaseClient;
    
    @BeforeEach
    void setUp() {
        // Clean database before each test
        databaseClient.sql("DELETE FROM devices")
                .fetch()
                .rowsUpdated()
                .block();
    }
    
    @Test
    @DisplayName("Should save a new device and return it with generated ID")
    void shouldSaveNewDevice() {
        // Given
        Device newDevice = DeviceFixture.createAvailableDevice("Test Device", "Test Brand");
        
        // When
        Mono<Device> result = deviceRepository.save(newDevice);
        
        // Then
        StepVerifier.create(result)
                .assertNext(device -> {
                    assertThat(device.id()).isNotNull();
                    assertThat(device.name()).isEqualTo("Test Device");
                    assertThat(device.brand()).isEqualTo("Test Brand");
                    assertThat(device.state()).isEqualTo(DeviceState.AVAILABLE);
                    assertThat(device.createdAt()).isNotNull();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should find device by ID")
    void shouldFindDeviceById() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // When
        Mono<Device> result = deviceRepository.findById(saved.id());
        
        // Then
        StepVerifier.create(result)
                .assertNext(device -> {
                    assertThat(device.id()).isEqualTo(saved.id());
                    assertThat(device.name()).isEqualTo(saved.name());
                    assertThat(device.brand()).isEqualTo(saved.brand());
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should return empty when device not found")
    void shouldReturnEmptyWhenDeviceNotFound() {
        // When
        Mono<Device> result = deviceRepository.findById(999L);
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should find all devices ordered by created_at DESC")
    void shouldFindAllDevices() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 1", "Brand"))
                .block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 2", "Brand"))
                .block();
        
        // When
        List<Device> devices = deviceRepository.findAll()
                .collectList()
                .block();
        
        // Then
        assertThat(devices).hasSize(2);
        // Should be ordered by created_at DESC
        assertThat(devices.get(0).name()).isEqualTo("Device 2");
        assertThat(devices.get(1).name()).isEqualTo("Device 1");
    }
    
    @Test
    @DisplayName("Should find devices by brand")
    void shouldFindDevicesByBrand() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 1", "Apple")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 2", "Samsung")).block();
        deviceRepository.save(DeviceFixture.createAvailableDevice("Device 3", "Apple")).block();
        
        // When
        List<Device> devices = deviceRepository.findByBrand("Apple")
                .collectList()
                .block();
        
        // Then
        assertThat(devices).hasSize(2);
        assertThat(devices).extracting(Device::brand)
                .containsOnly("Apple");
    }
    
    @Test
    @DisplayName("Should find devices by state")
    void shouldFindDevicesByState() {
        // Given
        deviceRepository.save(DeviceFixture.createAvailableDevice()).block();
        Device inUse2 = DeviceFixture.createDeviceWithState("Device 2", "Brand 2", DeviceState.IN_USE);
        deviceRepository.save(inUse2).block();
        
        Device inUse3 = DeviceFixture.createDeviceWithState("Device 3", "Brand 3", DeviceState.IN_USE);
        deviceRepository.save(inUse3)
                .block();
        
        // When
        List<Device> devices = deviceRepository.findByState(DeviceState.IN_USE)
                .collectList()
                .block();
        
        // Then
        assertThat(devices).hasSize(2);
        assertThat(devices).extracting(Device::state)
                .containsOnly(DeviceState.IN_USE);
    }
    
    @Test
    @DisplayName("Should update existing device")
    void shouldUpdateExistingDevice() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        Device toUpdate = new Device(saved.id(), "Updated Name", "Updated Brand", DeviceState.IN_USE, saved.createdAt());
        
        // When
        Mono<Device> result = deviceRepository.save(toUpdate);
        
        // Then
        StepVerifier.create(result)
                .assertNext(device -> {
                    assertThat(device.id()).isEqualTo(saved.id());
                    assertThat(device.name()).isEqualTo("Updated Name");
                    assertThat(device.brand()).isEqualTo("Updated Brand");
                    assertThat(device.state()).isEqualTo(DeviceState.IN_USE);
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should delete device by ID")
    void shouldDeleteDeviceById() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // When
        Mono<Void> result = deviceRepository.deleteById(saved.id());
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        // Verify device is deleted
        StepVerifier.create(deviceRepository.findById(saved.id()))
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Should check if device exists by ID")
    void shouldCheckIfDeviceExists() {
        // Given
        Device saved = deviceRepository.save(DeviceFixture.createAvailableDevice())
                .block();
        
        // When & Then
        StepVerifier.create(deviceRepository.existsById(saved.id()))
                .assertNext(exists -> assertThat(exists).isTrue())
                .verifyComplete();
        
        StepVerifier.create(deviceRepository.existsById(999L))
                .assertNext(exists -> assertThat(exists).isFalse())
                .verifyComplete();
    }
}


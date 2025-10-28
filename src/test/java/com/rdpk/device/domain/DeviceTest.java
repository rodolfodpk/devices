package com.rdpk.device.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Device Domain Model Tests")
class DeviceTest {
    
    @Test
    @DisplayName("Should create device with AVAILABLE state by default")
    void shouldCreateDeviceWithAvailableState() {
        // Given & When
        Device device = new Device("Test Device", "Test Brand");
        
        // Then
        assertThat(device.id()).isNull();
        assertThat(device.name()).isEqualTo("Test Device");
        assertThat(device.brand()).isEqualTo("Test Brand");
        assertThat(device.state()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(device.createdAt()).isNotNull();
        assertThat(device.isInUse()).isFalse();
    }
    
    @Test
    @DisplayName("Should correctly identify IN_USE devices")
    void shouldIdentifyInUseDevices() {
        // Given
        Device availableDevice = new Device("Device 1", "Brand");
        Device inUseDevice = new Device("Device 2", "Brand").withState(DeviceState.IN_USE);
        
        // Then
        assertThat(availableDevice.isInUse()).isFalse();
        assertThat(inUseDevice.isInUse()).isTrue();
    }
    
    @Test
    @DisplayName("Should create new instance when updating state")
    void shouldCreateNewInstanceWhenUpdatingState() {
        // Given
        Device original = new Device("Original", "Brand");
        
        // When
        Device updated = original.withState(DeviceState.IN_USE);
        
        // Then
        assertThat(updated).isNotSameAs(original);
        assertThat(updated.state()).isEqualTo(DeviceState.IN_USE);
        assertThat(original.state()).isEqualTo(DeviceState.AVAILABLE); // Original unchanged
    }
    
    @Test
    @DisplayName("Should create new instance when updating name")
    void shouldCreateNewInstanceWhenUpdatingName() {
        // Given
        Device original = new Device("Original", "Brand");
        
        // When
        Device updated = original.withName("Updated");
        
        // Then
        assertThat(updated).isNotSameAs(original);
        assertThat(updated.name()).isEqualTo("Updated");
        assertThat(original.name()).isEqualTo("Original"); // Original unchanged
    }
    
    @Test
    @DisplayName("Should create new instance when updating brand")
    void shouldCreateNewInstanceWhenUpdatingBrand() {
        // Given
        Device original = new Device("Name", "Original Brand");
        
        // When
        Device updated = original.withBrand("Updated Brand");
        
        // Then
        assertThat(updated).isNotSameAs(original);
        assertThat(updated.brand()).isEqualTo("Updated Brand");
        assertThat(original.brand()).isEqualTo("Original Brand"); // Original unchanged
    }
    
    @Test
    @DisplayName("Should create new instance when updating both name and brand")
    void shouldCreateNewInstanceWhenUpdatingBothNameAndBrand() {
        // Given
        Device original = new Device("Original Name", "Original Brand");
        
        // When
        Device updated = original.withNameAndBrand("Updated Name", "Updated Brand");
        
        // Then
        assertThat(updated).isNotSameAs(original);
        assertThat(updated.name()).isEqualTo("Updated Name");
        assertThat(updated.brand()).isEqualTo("Updated Brand");
        assertThat(original.name()).isEqualTo("Original Name"); // Original unchanged
        assertThat(original.brand()).isEqualTo("Original Brand"); // Original unchanged
    }
    
    @Test
    @DisplayName("Should preserve original record when using withState")
    void shouldPreserveOriginalWhenUsingWithState() {
        // Given
        Device original = new Device("Device", "Brand");
        
        // When
        original.withState(DeviceState.IN_USE);
        
        // Then - verify original is unchanged (immutability)
        assertThat(original.name()).isEqualTo("Device");
        assertThat(original.state()).isEqualTo(DeviceState.AVAILABLE);
    }
    
    @Test
    @DisplayName("Should correctly identify inactive devices as not in use")
    void shouldIdentifyInactiveDevicesAsNotInUse() {
        // Given
        Device inactiveDevice = new Device("Device", "Brand").withState(DeviceState.INACTIVE);
        
        // Then
        assertThat(inactiveDevice.isInUse()).isFalse();
        assertThat(inactiveDevice.state()).isEqualTo(DeviceState.INACTIVE);
    }
    
    @Test
    @DisplayName("Should correctly identify isDeletable for all states")
    void shouldCorrectlyIdentifyIsDeletableForAllStates() {
        // Given
        Device availableDevice = new Device("Available", "Brand");
        Device inUseDevice = new Device("InUse", "Brand").withState(DeviceState.IN_USE);
        Device inactiveDevice = new Device("Inactive", "Brand").withState(DeviceState.INACTIVE);
        
        // Then
        assertThat(availableDevice.isDeletable()).isTrue(); // AVAILABLE can be deleted
        assertThat(inUseDevice.isDeletable()).isFalse(); // IN_USE cannot be deleted
        assertThat(inactiveDevice.isDeletable()).isFalse(); // INACTIVE cannot be deleted
    }
    
    @Test
    @DisplayName("Should allow state transition to INACTIVE")
    void shouldAllowStateTransitionToInactive() {
        // Given
        Device availableDevice = new Device("Device", "Brand");
        
        // When
        Device inactiveDevice = availableDevice.withState(DeviceState.INACTIVE);
        
        // Then
        assertThat(inactiveDevice.state()).isEqualTo(DeviceState.INACTIVE);
        assertThat(availableDevice.state()).isEqualTo(DeviceState.AVAILABLE); // Original unchanged
    }
    
    @Test
    @DisplayName("Should allow state transition from INACTIVE")
    void shouldAllowStateTransitionFromInactive() {
        // Given
        Device inactiveDevice = new Device("Device", "Brand").withState(DeviceState.INACTIVE);
        
        // When - reactivate device
        Device reactivatedDevice = inactiveDevice.withState(DeviceState.AVAILABLE);
        
        // Then
        assertThat(reactivatedDevice.state()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(inactiveDevice.state()).isEqualTo(DeviceState.INACTIVE); // Original unchanged
    }
    
    @Test
    @DisplayName("Should test isInUse() for all three states")
    void shouldTestIsInUseForAllStates() {
        // Given
        Device availableDevice = new Device("Available", "Brand");
        Device inUseDevice = new Device("InUse", "Brand").withState(DeviceState.IN_USE);
        Device inactiveDevice = new Device("Inactive", "Brand").withState(DeviceState.INACTIVE);
        
        // Then
        assertThat(availableDevice.isInUse()).isFalse();
        assertThat(inUseDevice.isInUse()).isTrue();
        assertThat(inactiveDevice.isInUse()).isFalse();
    }
}


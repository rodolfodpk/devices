package com.rdpk.device.fixture;

import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;

import java.time.LocalDateTime;

public class DeviceFixture {
    
    public static Device createAvailableDevice() {
        return new Device("iPhone 15", "Apple", LocalDateTime.now());
    }
    
    public static Device createInUseDevice() {
        Device device = new Device("Samsung Galaxy S24", "Samsung", LocalDateTime.now());
        return device.withState(DeviceState.IN_USE);
    }
    
    public static Device createInactiveDevice() {
        Device device = new Device("Old Device", "Generic", LocalDateTime.now());
        return device.withState(DeviceState.INACTIVE);
    }
    
    public static Device createAvailableDevice(String name, String brand) {
        return new Device(name, brand, LocalDateTime.now());
    }
    
    public static Device createInactiveDevice(String name, String brand) {
        return new Device(name, brand, LocalDateTime.now()).withState(DeviceState.INACTIVE);
    }
    
    public static Device createDeviceWithState(String name, String brand, DeviceState state) {
        Device device = new Device(name, brand, LocalDateTime.now());
        return device.withState(state);
    }
    
    public static Device createDeviceWithState(String name, String brand, DeviceState state, LocalDateTime createdAt) {
        // Create device, then update state if needed (for testing with specific createdAt)
        Device device = new Device(null, name, brand, DeviceState.AVAILABLE, createdAt);
        if (state != DeviceState.AVAILABLE) {
            device = device.withState(state);
        }
        return device;
    }
}


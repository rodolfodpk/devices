package com.rdpk.features.device.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("devices")
public record Device(
    @Id
    @Column("id")
    Long id,
    @Column("name")
    String name,
    @Column("brand")
    String brand,
    @Column("state")
    DeviceState state,
    @Column("created_at")
    LocalDateTime createdAt
) {
    public Device(String name, String brand) {
        this(null, name, brand, DeviceState.AVAILABLE, LocalDateTime.now());
    }
    
    public Device withState(DeviceState newState) {
        return new Device(id, name, brand, newState, createdAt);
    }
    
    public Device withNameAndBrand(String newName, String newBrand) {
        return new Device(id, newName, newBrand, state, createdAt);
    }
    
    public Device withName(String newName) {
        return new Device(id, newName, brand, state, createdAt);
    }
    
    public Device withBrand(String newBrand) {
        return new Device(id, name, newBrand, state, createdAt);
    }
    
    public boolean isInUse() {
        return state == DeviceState.IN_USE;
    }
}


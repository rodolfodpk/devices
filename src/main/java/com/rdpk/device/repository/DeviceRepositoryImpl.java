package com.rdpk.device.repository;

import com.rdpk.device.domain.Device;
import com.rdpk.device.domain.DeviceState;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class DeviceRepositoryImpl implements DeviceRepository {
    
    private final DatabaseClient databaseClient;
    
    public DeviceRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }
    
    @Override
    public Mono<Device> save(Device device) {
        if (device.id() == null) {
            return databaseClient.sql("""
                    INSERT INTO devices (name, brand, state, created_at)
                    VALUES (:name, :brand, :state, :createdAt)
                    RETURNING id, name, brand, state, created_at
                    """)
                    .bind("name", device.name())
                    .bind("brand", device.brand())
                    .bind("state", device.state().name())
                    .bind("createdAt", device.createdAt())
                    .map(row -> new Device(
                        ((Number) row.get("id")).longValue(),
                        (String) row.get("name"),
                        (String) row.get("brand"),
                        DeviceState.valueOf((String) row.get("state")),
                        (java.time.LocalDateTime) row.get("created_at")
                    ))
                    .one();
        } else {
            return databaseClient.sql("""
                    UPDATE devices 
                    SET name = :name, brand = :brand, state = :state
                    WHERE id = :id
                    RETURNING id, name, brand, state, created_at
                    """)
                    .bind("id", device.id())
                    .bind("name", device.name())
                    .bind("brand", device.brand())
                    .bind("state", device.state().name())
                    .map(row -> new Device(
                        ((Number) row.get("id")).longValue(),
                        (String) row.get("name"),
                        (String) row.get("brand"),
                        DeviceState.valueOf((String) row.get("state")),
                        (java.time.LocalDateTime) row.get("created_at")
                    ))
                    .one();
        }
    }
    
    @Override
    public Mono<Device> findById(Long id) {
        return databaseClient.sql("""
                SELECT id, name, brand, state, created_at
                FROM devices
                WHERE id = :id
                """)
                .bind("id", id)
                .map(row -> new Device(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("name"),
                    (String) row.get("brand"),
                    DeviceState.valueOf((String) row.get("state")),
                    (java.time.LocalDateTime) row.get("created_at")
                ))
                .one()
                .switchIfEmpty(Mono.empty());
    }
    
    @Override
    public Mono<Void> deleteById(Long id) {
        return databaseClient.sql("DELETE FROM devices WHERE id = :id")
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .then();
    }
    
    @Override
    public Flux<Device> findAll() {
        return databaseClient.sql("""
                SELECT id, name, brand, state, created_at
                FROM devices
                ORDER BY created_at DESC
                """)
                .map(row -> new Device(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("name"),
                    (String) row.get("brand"),
                    DeviceState.valueOf((String) row.get("state")),
                    (java.time.LocalDateTime) row.get("created_at")
                ))
                .all();
    }
    
    @Override
    public Flux<Device> findByBrand(String brand) {
        return databaseClient.sql("""
                SELECT id, name, brand, state, created_at
                FROM devices
                WHERE brand = :brand
                ORDER BY created_at DESC
                """)
                .bind("brand", brand)
                .map(row -> new Device(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("name"),
                    (String) row.get("brand"),
                    DeviceState.valueOf((String) row.get("state")),
                    (java.time.LocalDateTime) row.get("created_at")
                ))
                .all();
    }
    
    @Override
    public Flux<Device> findByState(DeviceState state) {
        return databaseClient.sql("""
                SELECT id, name, brand, state, created_at
                FROM devices
                WHERE state = :state
                ORDER BY created_at DESC
                """)
                .bind("state", state.name())
                .map(row -> new Device(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("name"),
                    (String) row.get("brand"),
                    DeviceState.valueOf((String) row.get("state")),
                    (java.time.LocalDateTime) row.get("created_at")
                ))
                .all();
    }
    
    @Override
    public Mono<Boolean> existsById(Long id) {
        return databaseClient.sql("SELECT COUNT(*) FROM devices WHERE id = :id")
                .bind("id", id)
                .map(row -> ((Number) row.get("count")).intValue() > 0)
                .one();
    }
}


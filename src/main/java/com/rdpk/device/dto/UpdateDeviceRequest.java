package com.rdpk.device.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for partial device updates (PATCH operation).
 * 
 * <p>All fields are optional. Only non-null fields provided in the request 
 * will be updated. Fields that are null or omitted will remain unchanged 
 * in the database.
 * 
 * <p>Example partial updates:
 * <ul>
 *   <li>Update only state: {"state": "IN_USE"}</li>
 *   <li>Update name and brand: {"name": "iPhone 15", "brand": "Apple"}</li>
 *   <li>Update all fields: {"name": "iPhone 15", "brand": "Apple", "state": "AVAILABLE"}</li>
 * </ul>
 */
public record UpdateDeviceRequest(
    /** Device name. Null or omitted = field not updated. */
    @Size(max = 100, message = "Device name must not exceed 100 characters")
    String name,
    
    /** Device brand. Null or omitted = field not updated. */
    @Size(max = 50, message = "Device brand must not exceed 50 characters")
    String brand,
    
    /** Device state (AVAILABLE, IN_USE, INACTIVE). Null or omitted = field not updated. */
    String state
) {}


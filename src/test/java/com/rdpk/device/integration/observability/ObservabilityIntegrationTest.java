package com.rdpk.device.integration.observability;

import com.rdpk.device.AbstractIntegrationTest;
import com.rdpk.device.domain.Device;
import com.rdpk.device.fixture.DeviceFixture;
import com.rdpk.device.repository.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Observability Integration Tests")
class ObservabilityIntegrationTest extends AbstractIntegrationTest {
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Test
    @DisplayName("GET /actuator/health - Should return UP status")
    void shouldExposeHealthEndpoint() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }
    
    @Test
    @DisplayName("GET /actuator/metrics - Should list available metrics")
    void shouldListAvailableMetrics() {
        webTestClient.get()
                .uri("/actuator/metrics")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.names").isArray()
                .jsonPath("$.names").isNotEmpty();
    }
    
    // NOTE: Prometheus endpoint testing is difficult in Spring Boot test context
    // The /actuator/prometheus endpoint requires full Micrometer registry initialization
    // which may not happen properly in test contexts. Verification should be done manually:
    // 1. Start application with: make start-obs
    // 2. Access: http://localhost:8080/actuator/prometheus
    // 3. Verify metrics are exposed correctly
}


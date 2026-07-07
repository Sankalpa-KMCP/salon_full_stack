package com.velvetsalon.api.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class ServiceRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    void canSaveAndRetrieveServiceBySlug() {
        ServiceEntity service = new ServiceEntity();
        service.setSlug("haircut-mens");
        service.setName("Men's Haircut");
        service.setDescription("A classic men's haircut.");
        service.setPrice(new BigDecimal("35.00"));
        service.setDurationMinutes(30);

        ServiceEntity saved = serviceRepository.save(service);
        assertNotNull(saved.getId());

        Optional<ServiceEntity> retrievedOpt = serviceRepository.findBySlug("haircut-mens");
        assertTrue(retrievedOpt.isPresent());
        ServiceEntity retrieved = retrievedOpt.get();
        
        assertEquals("Men's Haircut", retrieved.getName());
        assertEquals(0, new BigDecimal("35.00").compareTo(retrieved.getPrice()));
        assertEquals(30, retrieved.getDurationMinutes());
    }
}

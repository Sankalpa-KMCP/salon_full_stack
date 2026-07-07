package com.velvetsalon.api.domain.staff;

import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
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
class StaffRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    @Transactional
    void canSaveAndRetrieveStaffWithServices() {
        // 1. Create and save a Service
        ServiceEntity service = new ServiceEntity();
        service.setSlug("haircut");
        service.setName("Haircut");
        service.setPrice(new BigDecimal("50.00"));
        service.setDurationMinutes(45);
        service = serviceRepository.save(service);

        // 2. Create and save a Staff Member
        StaffEntity staff = new StaffEntity();
        staff.setSlug("jane-doe");
        staff.setName("Jane Doe");
        staff.setRole("Senior Stylist");
        staff.setSpecialty("Coloring");
        staff.getServices().add(service);

        StaffEntity savedStaff = staffRepository.save(staff);
        assertNotNull(savedStaff.getId());

        // 3. Retrieve and verify
        Optional<StaffEntity> retrievedOpt = staffRepository.findBySlug("jane-doe");
        assertTrue(retrievedOpt.isPresent());
        
        StaffEntity retrieved = retrievedOpt.get();
        assertEquals("Jane Doe", retrieved.getName());
        assertEquals("Senior Stylist", retrieved.getRole());
        assertEquals("Coloring", retrieved.getSpecialty());
        assertTrue(retrieved.getIsActive());
        assertNotNull(retrieved.getCreatedAt());

        // 4. Verify Many-to-Many mapping via staff_services
        assertEquals(1, retrieved.getServices().size());
        ServiceEntity mappedService = retrieved.getServices().iterator().next();
        assertEquals("haircut", mappedService.getSlug());
    }
}

package com.velvetsalon.api.domain.appointment;

import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class AppointmentRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private StaffEntity testStaff;
    private ServiceEntity testService;

    @BeforeEach
    void setUp() {
        // Create standard staff fixture
        StaffEntity staff = new StaffEntity();
        staff.setSlug("jessica-stylist");
        staff.setName("Jessica Stylist");
        staff.setRole("Stylist");
        testStaff = staffRepository.save(staff);

        // Create standard service fixture
        ServiceEntity service = new ServiceEntity();
        service.setSlug("haircut");
        service.setName("Haircut");
        service.setDurationMinutes(30);
        service.setPrice(BigDecimal.valueOf(45.00));
        testService = serviceRepository.save(service);
    }

    @Test
    @Transactional
    void canSaveAndRetrieveAppointmentCorrectly() {
        OffsetDateTime startTime = OffsetDateTime.of(2026, 7, 10, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endTime = OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app = new AppointmentEntity();
        app.setCustomerName("Alice Smith");
        app.setCustomerEmail("alice@example.com");
        app.setCustomerPhone("+1234567890");
        app.setNotes("Prefer styling window.");
        app.setService(testService);
        app.setStaff(testStaff);
        app.setStartTime(startTime);
        app.setEndTime(endTime);
        app.setStatus(AppointmentStatus.PENDING);

        AppointmentEntity saved = appointmentRepository.saveAndFlush(app);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCancellationToken());
        assertNotNull(saved.getCreatedAt());

        Optional<AppointmentEntity> retrievedOpt = appointmentRepository.findById(saved.getId());
        assertTrue(retrievedOpt.isPresent());
        AppointmentEntity retrieved = retrievedOpt.get();

        assertEquals("Alice Smith", retrieved.getCustomerName());
        assertEquals("alice@example.com", retrieved.getCustomerEmail());
        assertEquals("+1234567890", retrieved.getCustomerPhone());
        assertEquals("Prefer styling window.", retrieved.getNotes());
        assertEquals(AppointmentStatus.PENDING, retrieved.getStatus());
        assertTrue(startTime.isEqual(retrieved.getStartTime()));
        assertTrue(endTime.isEqual(retrieved.getEndTime()));
    }

    @Test
    @Transactional
    void canFindByCancellationToken() {
        OffsetDateTime startTime = OffsetDateTime.of(2026, 7, 10, 11, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endTime = OffsetDateTime.of(2026, 7, 10, 11, 30, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app = new AppointmentEntity();
        app.setCustomerName("Bob Jones");
        app.setCustomerEmail("bob@example.com");
        app.setCustomerPhone("+9876543210");
        app.setService(testService);
        app.setStaff(testStaff);
        app.setStartTime(startTime);
        app.setEndTime(endTime);
        app.setStatus(AppointmentStatus.CONFIRMED);

        AppointmentEntity saved = appointmentRepository.saveAndFlush(app);
        UUID token = saved.getCancellationToken();
        assertNotNull(token);

        Optional<AppointmentEntity> retrievedOpt = appointmentRepository.findByCancellationToken(token);
        assertTrue(retrievedOpt.isPresent());
        assertEquals(saved.getId(), retrievedOpt.get().getId());
    }

    @Test
    @Transactional
    void rejectsInvalidEndTimeBeforeStartTime() {
        OffsetDateTime startTime = OffsetDateTime.of(2026, 7, 10, 13, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endTime = OffsetDateTime.of(2026, 7, 10, 12, 0, 0, 0, ZoneOffset.UTC); // Invalid end time

        AppointmentEntity app = new AppointmentEntity();
        app.setCustomerName("Charlie");
        app.setCustomerEmail("charlie@example.com");
        app.setCustomerPhone("+111111");
        app.setService(testService);
        app.setStaff(testStaff);
        app.setStartTime(startTime);
        app.setEndTime(endTime);
        app.setStatus(AppointmentStatus.PENDING);

        assertThrows(DataIntegrityViolationException.class, () -> {
            appointmentRepository.saveAndFlush(app);
        });
    }

    @Test
    @Transactional
    void rejectsDuplicateCancellationToken() {
        OffsetDateTime t1_start = OffsetDateTime.of(2026, 7, 10, 14, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t1_end = OffsetDateTime.of(2026, 7, 10, 14, 30, 0, 0, ZoneOffset.UTC);
        UUID sharedToken = UUID.randomUUID();

        AppointmentEntity app1 = new AppointmentEntity();
        app1.setCustomerName("User One");
        app1.setCustomerEmail("one@example.com");
        app1.setCustomerPhone("+22222");
        app1.setService(testService);
        app1.setStaff(testStaff);
        app1.setStartTime(t1_start);
        app1.setEndTime(t1_end);
        app1.setStatus(AppointmentStatus.PENDING);
        app1.setCancellationToken(sharedToken);
        appointmentRepository.saveAndFlush(app1);

        OffsetDateTime t2_start = OffsetDateTime.of(2026, 7, 10, 15, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t2_end = OffsetDateTime.of(2026, 7, 10, 15, 30, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app2 = new AppointmentEntity();
        app2.setCustomerName("User Two");
        app2.setCustomerEmail("two@example.com");
        app2.setCustomerPhone("+33333");
        app2.setService(testService);
        app2.setStaff(testStaff);
        app2.setStartTime(t2_start);
        app2.setEndTime(t2_end);
        app2.setStatus(AppointmentStatus.PENDING);
        app2.setCancellationToken(sharedToken); // Duplicate token

        assertThrows(DataIntegrityViolationException.class, () -> {
            appointmentRepository.saveAndFlush(app2);
        });
    }

    @Test
    @Transactional
    void rejectsOverlappingActiveAppointmentsForSameStaff() {
        // App 1: 10:00 - 11:00 PENDING
        OffsetDateTime app1Start = OffsetDateTime.of(2026, 7, 12, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime app1End = OffsetDateTime.of(2026, 7, 12, 11, 0, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app1 = new AppointmentEntity();
        app1.setCustomerName("User One");
        app1.setCustomerEmail("one@example.com");
        app1.setCustomerPhone("+111");
        app1.setService(testService);
        app1.setStaff(testStaff);
        app1.setStartTime(app1Start);
        app1.setEndTime(app1End);
        app1.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.saveAndFlush(app1);

        // App 2: Overlapping 10:30 - 11:30 CONFIRMED
        OffsetDateTime app2Start = OffsetDateTime.of(2026, 7, 12, 10, 30, 0, 0, ZoneOffset.UTC);
        OffsetDateTime app2End = OffsetDateTime.of(2026, 7, 12, 11, 30, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app2 = new AppointmentEntity();
        app2.setCustomerName("User Two");
        app2.setCustomerEmail("two@example.com");
        app2.setCustomerPhone("+222");
        app2.setService(testService);
        app2.setStaff(testStaff);
        app2.setStartTime(app2Start);
        app2.setEndTime(app2End);
        app2.setStatus(AppointmentStatus.CONFIRMED);

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class, () -> {
            appointmentRepository.saveAndFlush(app2);
        });

        // Verify it was triggered by the exclusion constraint
        String rootMsg = ex.getMostSpecificCause().getMessage();
        assertTrue(rootMsg.contains("no_double_booking") || rootMsg.contains("23P01"),
                "Expected conflict exception due to 'no_double_booking' or PostgreSQL exclusion state 23P01. Found: " + rootMsg);
    }

    @Test
    @Transactional
    void allowsAdjacentAppointmentsForSameStaff() {
        // App 1: 10:00 - 11:00 PENDING
        OffsetDateTime app1Start = OffsetDateTime.of(2026, 7, 13, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime app1End = OffsetDateTime.of(2026, 7, 13, 11, 0, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app1 = new AppointmentEntity();
        app1.setCustomerName("User One");
        app1.setCustomerEmail("one@example.com");
        app1.setCustomerPhone("+111");
        app1.setService(testService);
        app1.setStaff(testStaff);
        app1.setStartTime(app1Start);
        app1.setEndTime(app1End);
        app1.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.saveAndFlush(app1);

        // App 2: Adjacent 11:00 - 12:00 CONFIRMED
        OffsetDateTime app2Start = OffsetDateTime.of(2026, 7, 13, 11, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime app2End = OffsetDateTime.of(2026, 7, 13, 12, 0, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app2 = new AppointmentEntity();
        app2.setCustomerName("User Two");
        app2.setCustomerEmail("two@example.com");
        app2.setCustomerPhone("+222");
        app2.setService(testService);
        app2.setStaff(testStaff);
        app2.setStartTime(app2Start);
        app2.setEndTime(app2End);
        app2.setStatus(AppointmentStatus.CONFIRMED);

        assertDoesNotThrow(() -> {
            appointmentRepository.saveAndFlush(app2);
        });
    }

    @Test
    @Transactional
    void allowsOverlappingWithCancelledOrCompletedAppointments() {
        // App 1: 10:00 - 11:00 CANCELLED
        OffsetDateTime app1Start = OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime app1End = OffsetDateTime.of(2026, 7, 14, 11, 0, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app1 = new AppointmentEntity();
        app1.setCustomerName("User One");
        app1.setCustomerEmail("one@example.com");
        app1.setCustomerPhone("+111");
        app1.setService(testService);
        app1.setStaff(testStaff);
        app1.setStartTime(app1Start);
        app1.setEndTime(app1End);
        app1.setStatus(AppointmentStatus.CANCELLED); // Non-active status
        appointmentRepository.saveAndFlush(app1);

        // App 2: Overlapping 10:30 - 11:30 CONFIRMED
        OffsetDateTime app2Start = OffsetDateTime.of(2026, 7, 14, 10, 30, 0, 0, ZoneOffset.UTC);
        OffsetDateTime app2End = OffsetDateTime.of(2026, 7, 14, 11, 30, 0, 0, ZoneOffset.UTC);

        AppointmentEntity app2 = new AppointmentEntity();
        app2.setCustomerName("User Two");
        app2.setCustomerEmail("two@example.com");
        app2.setCustomerPhone("+222");
        app2.setService(testService);
        app2.setStaff(testStaff);
        app2.setStartTime(app2Start);
        app2.setEndTime(app2End);
        app2.setStatus(AppointmentStatus.CONFIRMED);

        assertDoesNotThrow(() -> {
            appointmentRepository.saveAndFlush(app2);
        });
    }
}

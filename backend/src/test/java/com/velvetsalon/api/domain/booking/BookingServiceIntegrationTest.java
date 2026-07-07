package com.velvetsalon.api.domain.booking;

import com.velvetsalon.api.domain.appointment.AppointmentEntity;
import com.velvetsalon.api.domain.appointment.AppointmentRepository;
import com.velvetsalon.api.domain.appointment.AppointmentStatus;
import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.BlockedTimeEntity;
import com.velvetsalon.api.domain.staff.BlockedTimeRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import com.velvetsalon.api.domain.staff.WorkingHoursEntity;
import com.velvetsalon.api.domain.staff.WorkingHoursRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class BookingServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @Autowired
    private BlockedTimeRepository blockedTimeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BookingService bookingService;

    private StaffEntity stylistA; // jessica-stylist
    private StaffEntity stylistB; // alice-stylist
    private ServiceEntity haircutService;
    private ServiceEntity inactiveService;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        blockedTimeRepository.deleteAll();
        workingHoursRepository.deleteAll();
        staffRepository.deleteAll();
        serviceRepository.deleteAll();

        // 1. Create stylists
        StaffEntity staffA = new StaffEntity();
        staffA.setSlug("jessica-stylist");
        staffA.setName("Jessica Stylist");
        staffA.setRole("Senior Stylist");
        staffA.setIsActive(true);
        stylistA = staffRepository.save(staffA);

        StaffEntity staffB = new StaffEntity();
        staffB.setSlug("alice-stylist");
        staffB.setName("Alice Stylist");
        staffB.setRole("Junior Stylist");
        staffB.setIsActive(true);
        stylistB = staffRepository.save(staffB);

        // 2. Create services
        ServiceEntity s1 = new ServiceEntity();
        s1.setSlug("haircut-30");
        s1.setName("Haircut 30");
        s1.setDurationMinutes(30);
        s1.setPrice(BigDecimal.valueOf(40.00));
        s1.setIsActive(true);
        haircutService = serviceRepository.save(s1);

        ServiceEntity s2 = new ServiceEntity();
        s2.setSlug("coloring-60");
        s2.setName("Coloring 60");
        s2.setDurationMinutes(60);
        s2.setPrice(BigDecimal.valueOf(100.00));
        s2.setIsActive(false); // Inactive
        inactiveService = serviceRepository.save(s2);

        // Qualify both stylists for haircutService
        stylistA.getServices().add(haircutService);
        stylistA = staffRepository.saveAndFlush(stylistA);

        stylistB.getServices().add(haircutService);
        stylistB = staffRepository.saveAndFlush(stylistB);
    }

    @Test
    @Transactional
    void validSpecificStylistBookingPersistsCorrectly() {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(stylistA);
        wh.setDayOfWeek(dayOfWeek);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        AppointmentEntity saved = bookingService.createAppointment(
                haircutService.getSlug(),
                stylistA.getSlug(),
                requestedStart,
                "John Doe",
                "john@example.com",
                "+111222333",
                "Likes scissors over clipper"
        );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCancellationToken());
        assertEquals(AppointmentStatus.CONFIRMED, saved.getStatus());
        assertEquals("John Doe", saved.getCustomerName());
        assertEquals("john@example.com", saved.getCustomerEmail());
        assertEquals("+111222333", saved.getCustomerPhone());
        assertEquals("Likes scissors over clipper", saved.getNotes());
        assertEquals(stylistA.getId(), saved.getStaff().getId());
        assertEquals(haircutService.getId(), saved.getService().getId());

        Instant expectedEnd = date.atTime(10, 30).atZone(COLOMBO_ZONE).toInstant();
        assertEquals(expectedEnd, saved.getEndTime().toInstant());
        assertEquals(requestedStart, saved.getStartTime().toInstant());
    }

    @Test
    @Transactional
    void invalidOrUnavailableNamedStylistRequestRejected() {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        // 1. Stylist has no working hours -> unavailable
        assertThrows(BookingValidationException.class, () -> {
            bookingService.createAppointment(
                    haircutService.getSlug(),
                    stylistA.getSlug(),
                    requestedStart,
                    "John Doe",
                    "john@example.com",
                    "+111222333",
                    "Notes"
            );
        });

        // 2. Stylist has working hours but block exists
        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(stylistA);
        wh.setDayOfWeek(dayOfWeek);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        BlockedTimeEntity block = new BlockedTimeEntity();
        block.setStaff(stylistA);
        block.setStartTime(date.atTime(9, 30).atZone(COLOMBO_ZONE).toOffsetDateTime());
        block.setEndTime(date.atTime(10, 30).atZone(COLOMBO_ZONE).toOffsetDateTime());
        block.setReason("Lunch block");
        blockedTimeRepository.saveAndFlush(block);

        assertThrows(BookingValidationException.class, () -> {
            bookingService.createAppointment(
                    haircutService.getSlug(),
                    stylistA.getSlug(),
                    requestedStart, // Overlaps blocked time
                    "John Doe",
                    "john@example.com",
                    "+111222333",
                    "Notes"
            );
        });
    }

    @Test
    @Transactional
    void inactiveMissingOrUnqualifiedCombinationRejected() {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(stylistA);
        wh.setDayOfWeek(dayOfWeek);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        // 1. Inactive service
        assertThrows(BookingValidationException.class, () -> {
            bookingService.createAppointment(
                    inactiveService.getSlug(),
                    stylistA.getSlug(),
                    requestedStart,
                    "John Doe",
                    "john@example.com",
                    "+111222333",
                    "Notes"
            );
        });

        // 2. Inactive stylist
        stylistA.setIsActive(false);
        staffRepository.saveAndFlush(stylistA);

        assertThrows(BookingValidationException.class, () -> {
            bookingService.createAppointment(
                    haircutService.getSlug(),
                    stylistA.getSlug(),
                    requestedStart,
                    "John Doe",
                    "john@example.com",
                    "+111222333",
                    "Notes"
            );
        });

        stylistA.setIsActive(true);
        staffRepository.saveAndFlush(stylistA);

        // 3. Unqualified stylist
        ServiceEntity unqualifiedS = new ServiceEntity();
        unqualifiedS.setSlug("unqualified-service");
        unqualifiedS.setName("Unqualified");
        unqualifiedS.setDurationMinutes(30);
        unqualifiedS.setPrice(BigDecimal.valueOf(30.00));
        unqualifiedS.setIsActive(true);
        final ServiceEntity savedUnqualified = serviceRepository.save(unqualifiedS);

        assertThrows(BookingValidationException.class, () -> {
            bookingService.createAppointment(
                    savedUnqualified.getSlug(),
                    stylistA.getSlug(),
                    requestedStart,
                    "John Doe",
                    "john@example.com",
                    "+111222333",
                    "Notes"
            );
        });
    }

    @Test
    @Transactional
    void bookingHorizonValidation() {
        LocalDate today = LocalDate.now(COLOMBO_ZONE);

        // 1. Rejects date before Colombo today
        Instant beforeToday = today.minusDays(1).atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();
        assertThrows(BookingValidationException.class, () -> {
            bookingService.createAppointment(
                    haircutService.getSlug(),
                    stylistA.getSlug(),
                    beforeToday,
                    "John Doe",
                    "john@example.com",
                    "+111222333",
                    "Notes"
            );
        });

        // 2. Rejects date after Colombo today + 30 days
        Instant after30Days = today.plusDays(31).atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();
        assertThrows(BookingValidationException.class, () -> {
            bookingService.createAppointment(
                    haircutService.getSlug(),
                    stylistA.getSlug(),
                    after30Days,
                    "John Doe",
                    "john@example.com",
                    "+111222333",
                    "Notes"
            );
        });
    }

    @Test
    @Transactional
    void staffSlugAnySelectsFirstAvailableByAscendingSlug() {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity whA = new WorkingHoursEntity();
        whA.setStaff(stylistA);
        whA.setDayOfWeek(dayOfWeek);
        whA.setStartTime(LocalTime.of(9, 0));
        whA.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(whA);

        WorkingHoursEntity whB = new WorkingHoursEntity();
        whB.setStaff(stylistB);
        whB.setDayOfWeek(dayOfWeek);
        whB.setStartTime(LocalTime.of(9, 0));
        whB.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(whB);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        // Ascending slug order: "alice-stylist" (stylistB) then "jessica-stylist" (stylistA)
        AppointmentEntity saved = bookingService.createAppointment(
                haircutService.getSlug(),
                "any",
                requestedStart,
                "Customer Any",
                "any@example.com",
                "+9999",
                "Notes"
        );

        assertEquals(stylistB.getId(), saved.getStaff().getId(), "Alice should be selected as she has the first ascending slug.");
    }

    @Test
    @Transactional
    void staffSlugAnySkipsUnavailableStylistAndSelectsNext() {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity whA = new WorkingHoursEntity();
        whA.setStaff(stylistA);
        whA.setDayOfWeek(dayOfWeek);
        whA.setStartTime(LocalTime.of(9, 0));
        whA.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(whA);

        WorkingHoursEntity whB = new WorkingHoursEntity();
        whB.setStaff(stylistB);
        whB.setDayOfWeek(dayOfWeek);
        whB.setStartTime(LocalTime.of(9, 0));
        whB.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(whB);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        // Block Alice (stylistB, first in ascending slug order) during the requested time
        BlockedTimeEntity blockB = new BlockedTimeEntity();
        blockB.setStaff(stylistB);
        blockB.setStartTime(date.atTime(9, 30).atZone(COLOMBO_ZONE).toOffsetDateTime());
        blockB.setEndTime(date.atTime(10, 30).atZone(COLOMBO_ZONE).toOffsetDateTime());
        blockB.setReason("Doctor visit");
        blockedTimeRepository.saveAndFlush(blockB);

        // Alice is blocked, so Jessica (stylistA) must be selected
        AppointmentEntity saved = bookingService.createAppointment(
                haircutService.getSlug(),
                "any",
                requestedStart,
                "Customer Any",
                "any@example.com",
                "+9999",
                "Notes"
        );

        assertEquals(stylistA.getId(), saved.getStaff().getId(), "Jessica should be selected because Alice is blocked.");
    }

    @Test
    void concurrentBookingsForSameSlotTriggersConflictException() throws Exception {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(stylistA);
        wh.setDayOfWeek(dayOfWeek);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(2);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.atomic.AtomicReference<AppointmentEntity> successAppointment = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<Exception> failureException = new java.util.concurrent.atomic.AtomicReference<>();

        Runnable task = () -> {
            try {
                barrier.await(5, java.util.concurrent.TimeUnit.SECONDS);
                AppointmentEntity app = bookingService.createAppointment(
                        haircutService.getSlug(),
                        stylistA.getSlug(),
                        requestedStart,
                        "Concurrent Customer",
                        "concurrent@example.com",
                        "+12345",
                        "Thread Note"
                );
                successAppointment.set(app);
            } catch (Exception e) {
                failureException.set(e);
            } finally {
                latch.countDown();
            }
        };

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            executor.submit(task);
            executor.submit(task);

            boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            assertTrue(completed, "Concurrency tasks timed out");

            AppointmentEntity app = successAppointment.get();
            Exception ex = failureException.get();

            assertNotNull(app, "One appointment should have succeeded");
            assertNotNull(ex, "One appointment should have failed with double booking exception");
            assertTrue(ex instanceof DoubleBookingException, "Failure exception should be DoubleBookingException but was " + ex);

            long count = appointmentRepository.count();
            assertEquals(1, count, "Exactly 1 appointment should be persisted in database");
        } finally {
            executor.shutdownNow();
            appointmentRepository.deleteAll();
            blockedTimeRepository.deleteAll();
            workingHoursRepository.deleteAll();
            staffRepository.deleteAll();
            serviceRepository.deleteAll();
        }
    }
}

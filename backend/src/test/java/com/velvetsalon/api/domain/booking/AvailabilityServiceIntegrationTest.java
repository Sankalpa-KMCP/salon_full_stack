package com.velvetsalon.api.domain.booking;

import com.velvetsalon.api.domain.appointment.AppointmentEntity;
import com.velvetsalon.api.domain.appointment.AppointmentRepository;
import com.velvetsalon.api.domain.appointment.AppointmentStatus;
import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.*;
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
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class AvailabilityServiceIntegrationTest {

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
    private AvailabilityService availabilityService;

    private StaffEntity testStaff;
    private ServiceEntity testService; // 45 minutes
    private ServiceEntity unqualifiedService; // 30 minutes

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private static final ZoneOffset COLOMBO_OFFSET = ZoneOffset.ofHoursMinutes(5, 30);

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        blockedTimeRepository.deleteAll();
        workingHoursRepository.deleteAll();
        staffRepository.deleteAll();
        serviceRepository.deleteAll();

        // 1. Create stylist
        StaffEntity staff = new StaffEntity();
        staff.setSlug("jessica-stylist");
        staff.setName("Jessica Stylist");
        staff.setRole("Senior Stylist");
        testStaff = staffRepository.save(staff);

        // 2. Create qualified service (45 mins)
        ServiceEntity service = new ServiceEntity();
        service.setSlug("haircut-45");
        service.setName("Haircut 45");
        service.setDurationMinutes(45);
        service.setPrice(BigDecimal.valueOf(50.00));
        testService = serviceRepository.save(service);

        // Qualify stylist
        testStaff.getServices().add(testService);
        testStaff = staffRepository.saveAndFlush(testStaff);

        // 3. Create unqualified service
        ServiceEntity service2 = new ServiceEntity();
        service2.setSlug("massage-30");
        service2.setName("Massage 30");
        service2.setDurationMinutes(30);
        service2.setPrice(BigDecimal.valueOf(40.00));
        unqualifiedService = serviceRepository.save(service2);
    }

    @Test
    @Transactional
    void unqualifiedStaffServiceReturnsNoSlots() {
        LocalDate date = LocalDate.of(2026, 7, 10); // Friday

        // Save working hours for Friday
        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(testStaff);
        wh.setDayOfWeek(DayOfWeek.FRIDAY);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        List<AvailableSlot> slots = availabilityService.getAvailableSlots(
                unqualifiedService.getSlug(), testStaff.getSlug(), date);

        assertTrue(slots.isEmpty(), "Unqualified service should return no slots.");
    }

    @Test
    @Transactional
    void gridGenerationFor45MinServiceOn30MinIntervals() {
        LocalDate date = LocalDate.of(2026, 7, 10); // Friday (5)

        // Working hours: 09:00 to 11:00
        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(testStaff);
        wh.setDayOfWeek(DayOfWeek.FRIDAY);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(11, 0));
        workingHoursRepository.saveAndFlush(wh);

        List<AvailableSlot> slots = availabilityService.getAvailableSlots(
                testService.getSlug(), testStaff.getSlug(), date);

        // Expected slots (45 min duration, 30 min start steps):
        // 1. 09:00 - 09:45
        // 2. 09:30 - 10:15
        // 3. 10:00 - 10:45
        // (10:30 - 11:15 goes past 11:00 working-hours limit, so it should be excluded)
        assertEquals(3, slots.size());

        assertEquals(OffsetDateTime.of(2026, 7, 10, 9, 0, 0, 0, COLOMBO_OFFSET), slots.get(0).startTime());
        assertEquals(OffsetDateTime.of(2026, 7, 10, 9, 45, 0, 0, COLOMBO_OFFSET), slots.get(0).endTime());

        assertEquals(OffsetDateTime.of(2026, 7, 10, 9, 30, 0, 0, COLOMBO_OFFSET), slots.get(1).startTime());
        assertEquals(OffsetDateTime.of(2026, 7, 10, 10, 15, 0, 0, COLOMBO_OFFSET), slots.get(1).endTime());

        assertEquals(OffsetDateTime.of(2026, 7, 10, 10, 0, 0, 0, COLOMBO_OFFSET), slots.get(2).startTime());
        assertEquals(OffsetDateTime.of(2026, 7, 10, 10, 45, 0, 0, COLOMBO_OFFSET), slots.get(2).endTime());
    }

    @Test
    @Transactional
    void blockedTimeOverlapExcludesCandidateAndAdjacentBlockedTimeAllowed() {
        LocalDate date = LocalDate.of(2026, 7, 10); // Friday (5)

        // Working hours: 09:00 to 12:00
        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(testStaff);
        wh.setDayOfWeek(DayOfWeek.FRIDAY);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(12, 0));
        workingHoursRepository.saveAndFlush(wh);

        // Blocked time: 09:30 to 10:30
        BlockedTimeEntity bt = new BlockedTimeEntity();
        bt.setStaff(testStaff);
        bt.setStartTime(OffsetDateTime.of(2026, 7, 10, 9, 30, 0, 0, COLOMBO_OFFSET));
        bt.setEndTime(OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, COLOMBO_OFFSET));
        bt.setReason("Doctor visit");
        blockedTimeRepository.saveAndFlush(bt);

        List<AvailableSlot> slots = availabilityService.getAvailableSlots(
                testService.getSlug(), testStaff.getSlug(), date);

        // Candidate slots starting at:
        // - 09:00 -> 09:00 - 09:45 (Overlaps 09:30 - 10:30) -> EXCLUDED
        // - 09:30 -> 09:30 - 10:15 (Overlaps 09:30 - 10:30) -> EXCLUDED
        // - 10:00 -> 10:00 - 10:45 (Overlaps 09:30 - 10:30) -> EXCLUDED
        // - 10:30 -> 10:30 - 11:15 (Adjacent, no overlap) -> ALLOWED
        // - 11:00 -> 11:00 - 11:45 (Allowed)
        // (11:30 - 12:15 goes past 12:00 -> EXCLUDED)

        assertEquals(2, slots.size());

        assertEquals(OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, COLOMBO_OFFSET), slots.get(0).startTime());
        assertEquals(OffsetDateTime.of(2026, 7, 10, 11, 15, 0, 0, COLOMBO_OFFSET), slots.get(0).endTime());

        assertEquals(OffsetDateTime.of(2026, 7, 10, 11, 0, 0, 0, COLOMBO_OFFSET), slots.get(1).startTime());
        assertEquals(OffsetDateTime.of(2026, 7, 10, 11, 45, 0, 0, COLOMBO_OFFSET), slots.get(1).endTime());
    }

    @Test
    @Transactional
    void activeAppointmentOverlapExcludesCandidateAndCancelledCompletedAllowed() {
        LocalDate date = LocalDate.of(2026, 7, 10); // Friday (5)

        // Working hours: 09:00 to 12:00
        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(testStaff);
        wh.setDayOfWeek(DayOfWeek.FRIDAY);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(12, 0));
        workingHoursRepository.saveAndFlush(wh);

        // Active Appointment: 09:30 to 10:30 (CONFIRMED)
        AppointmentEntity app1 = new AppointmentEntity();
        app1.setCustomerName("Alice");
        app1.setCustomerEmail("alice@example.com");
        app1.setCustomerPhone("123");
        app1.setService(testService);
        app1.setStaff(testStaff);
        app1.setStartTime(OffsetDateTime.of(2026, 7, 10, 9, 30, 0, 0, COLOMBO_OFFSET));
        app1.setEndTime(OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, COLOMBO_OFFSET));
        app1.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.saveAndFlush(app1);

        // Inactive Appointment: 10:30 to 11:30 (CANCELLED)
        AppointmentEntity app2 = new AppointmentEntity();
        app2.setCustomerName("Bob");
        app2.setCustomerEmail("bob@example.com");
        app2.setCustomerPhone("456");
        app2.setService(testService);
        app2.setStaff(testStaff);
        app2.setStartTime(OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, COLOMBO_OFFSET));
        app2.setEndTime(OffsetDateTime.of(2026, 7, 10, 11, 30, 0, 0, COLOMBO_OFFSET));
        app2.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(app2);

        List<AvailableSlot> slots = availabilityService.getAvailableSlots(
                testService.getSlug(), testStaff.getSlug(), date);

        // Expected available slots:
        // - 10:30 - 11:15 (Overlaps only with CANCELLED appointment -> ALLOWED)
        // - 11:00 - 11:45 (Overlaps only with CANCELLED appointment -> ALLOWED)
        // (Other slots overlapping CONFIRMED [09:30 - 10:30) are excluded)

        assertEquals(2, slots.size());

        assertEquals(OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, COLOMBO_OFFSET), slots.get(0).startTime());
        assertEquals(OffsetDateTime.of(2026, 7, 10, 11, 0, 0, 0, COLOMBO_OFFSET), slots.get(1).startTime());
    }

    @Test
    @Transactional
    void colomboTimezoneBoundaryCrossingUTC() {
        LocalDate date = LocalDate.of(2026, 7, 10); // Friday (5)

        // Working hours: 09:00 to 12:00
        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(testStaff);
        wh.setDayOfWeek(DayOfWeek.FRIDAY);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(12, 0));
        workingHoursRepository.saveAndFlush(wh);

        // Blocked time crossing date boundary:
        // Local: 00:15 on 2026-07-10 -> UTC: 18:45 on 2026-07-09
        // This is on a different calendar day in UTC, but inside 2026-07-10 in Asia/Colombo.
        BlockedTimeEntity bt = new BlockedTimeEntity();
        bt.setStaff(testStaff);
        bt.setStartTime(OffsetDateTime.of(2026, 7, 10, 0, 0, 0, 0, COLOMBO_OFFSET));
        bt.setEndTime(OffsetDateTime.of(2026, 7, 10, 9, 30, 0, 0, COLOMBO_OFFSET));
        bt.setReason("Late Night Off");
        blockedTimeRepository.saveAndFlush(bt);

        List<AvailableSlot> slots = availabilityService.getAvailableSlots(
                testService.getSlug(), testStaff.getSlug(), date);

        // Candidate starting at 09:00 (ends 09:45) overlaps [00:00, 09:30] -> EXCLUDED
        // Candidate starting at 09:30 (ends 10:15) does not overlap -> ALLOWED
        // Candidate starting at 10:00 -> ALLOWED
        // Candidate starting at 10:30 -> ALLOWED
        // Candidate starting at 11:00 -> ALLOWED

        assertEquals(4, slots.size());
        assertEquals(OffsetDateTime.of(2026, 7, 10, 9, 30, 0, 0, COLOMBO_OFFSET), slots.get(0).startTime());
    }
}

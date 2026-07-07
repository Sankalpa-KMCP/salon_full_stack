package com.velvetsalon.api.domain.staff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class WorkingHoursRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @Test
    @Transactional
    void canSaveAndRetrieveWorkingHoursInCorrectOrder() {
        // 1. Create and save a Staff member
        StaffEntity staff = new StaffEntity();
        staff.setSlug("john-stylist");
        staff.setName("John Stylist");
        staff.setRole("Stylist");
        staff = staffRepository.save(staff);

        // 2. Create and save working hours for Monday and Sunday
        WorkingHoursEntity mondayHours = new WorkingHoursEntity();
        mondayHours.setStaff(staff);
        mondayHours.setDayOfWeek(DayOfWeek.MONDAY);
        mondayHours.setStartTime(LocalTime.of(9, 0));
        mondayHours.setEndTime(LocalTime.of(17, 0));

        WorkingHoursEntity sundayHours = new WorkingHoursEntity();
        sundayHours.setStaff(staff);
        sundayHours.setDayOfWeek(DayOfWeek.SUNDAY);
        sundayHours.setStartTime(LocalTime.of(10, 0));
        sundayHours.setEndTime(LocalTime.of(15, 0));

        workingHoursRepository.save(mondayHours);
        workingHoursRepository.save(sundayHours);

        // 3. Retrieve and assert order (Monday = 1, Sunday = 7)
        List<WorkingHoursEntity> hours = workingHoursRepository.findByStaffIdOrderByDayOfWeekAscStartTimeAsc(staff.getId());
        assertEquals(2, hours.size());

        WorkingHoursEntity first = hours.get(0);
        assertEquals(DayOfWeek.MONDAY, first.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), first.getStartTime());
        assertEquals(LocalTime.of(17, 0), first.getEndTime());

        WorkingHoursEntity second = hours.get(1);
        assertEquals(DayOfWeek.SUNDAY, second.getDayOfWeek());
        assertEquals(LocalTime.of(10, 0), second.getStartTime());
        assertEquals(LocalTime.of(15, 0), second.getEndTime());
    }

    @Test
    @Transactional
    void cannotSaveInvalidWorkingHoursEndTimeBeforeStartTime() {
        // 1. Create and save a Staff member
        StaffEntity staff = new StaffEntity();
        staff.setSlug("bob-stylist");
        staff.setName("Bob Stylist");
        staff.setRole("Stylist");
        staff = staffRepository.save(staff);

        // 2. Try saving working hours where end_time < start_time
        WorkingHoursEntity invalidHours = new WorkingHoursEntity();
        invalidHours.setStaff(staff);
        invalidHours.setDayOfWeek(DayOfWeek.TUESDAY);
        invalidHours.setStartTime(LocalTime.of(17, 0));
        invalidHours.setEndTime(LocalTime.of(9, 0)); // Invalid

        assertThrows(DataIntegrityViolationException.class, () -> {
            workingHoursRepository.saveAndFlush(invalidHours);
        });
    }
}

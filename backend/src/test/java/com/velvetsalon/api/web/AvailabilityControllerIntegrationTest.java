package com.velvetsalon.api.web;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
@Transactional
class AvailabilityControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

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

    private StaffEntity jessica;
    private StaffEntity kate;
    private ServiceEntity haircut;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private static final ZoneOffset COLOMBO_OFFSET = ZoneOffset.ofHoursMinutes(5, 30);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        appointmentRepository.deleteAll();
        blockedTimeRepository.deleteAll();
        workingHoursRepository.deleteAll();
        staffRepository.deleteAll();
        serviceRepository.deleteAll();

        // Create Jessica
        jessica = new StaffEntity();
        jessica.setSlug("jessica-stylist");
        jessica.setName("Jessica Stylist");
        jessica.setRole("Senior Stylist");
        jessica.setIsActive(true);
        jessica = staffRepository.save(jessica);

        // Create Kate
        kate = new StaffEntity();
        kate.setSlug("kate-stylist");
        kate.setName("Kate Stylist");
        kate.setRole("Stylist");
        kate.setIsActive(true);
        kate = staffRepository.save(kate);

        // Create Haircut Service (45 mins)
        haircut = new ServiceEntity();
        haircut.setSlug("haircut-45");
        haircut.setName("Haircut 45");
        haircut.setDurationMinutes(45);
        haircut.setPrice(BigDecimal.valueOf(50.00));
        haircut.setIsActive(true);
        haircut = serviceRepository.save(haircut);

        // Qualify both stylists
        jessica.getServices().add(haircut);
        jessica = staffRepository.saveAndFlush(jessica);

        kate.getServices().add(haircut);
        kate = staffRepository.saveAndFlush(kate);
    }

    @Test
    void specificStylistReturnsCorrectSlotsAndFields() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 10); // Friday

        // Setup working hours for Jessica: 09:00 - 11:00
        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(jessica);
        wh.setDayOfWeek(DayOfWeek.FRIDAY);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(11, 0));
        workingHoursRepository.saveAndFlush(wh);

        // Expected local slots:
        // 1. 09:00 - 09:45 -> UTC: 03:30 - 04:15
        // 2. 09:30 - 10:15 -> UTC: 04:00 - 04:45
        // 3. 10:00 - 10:45 -> UTC: 04:30 - 05:15

        mockMvc.perform(get("/api/booking/availability")
                        .param("serviceSlug", "haircut-45")
                        .param("staffSlug", "jessica-stylist")
                        .param("date", "2026-07-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceSlug", is("haircut-45")))
                .andExpect(jsonPath("$.staffSlug", is("jessica-stylist")))
                .andExpect(jsonPath("$.date", is("2026-07-10")))
                .andExpect(jsonPath("$.timeZone", is("Asia/Colombo")))
                .andExpect(jsonPath("$.slots", hasSize(3)))
                .andExpect(jsonPath("$.slots[0].startTime", is("2026-07-10T03:30:00Z")))
                .andExpect(jsonPath("$.slots[0].endTime", is("2026-07-10T04:15:00Z")))
                .andExpect(jsonPath("$.slots[1].startTime", is("2026-07-10T04:00:00Z")))
                .andExpect(jsonPath("$.slots[1].endTime", is("2026-07-10T04:45:00Z")))
                .andExpect(jsonPath("$.slots[2].startTime", is("2026-07-10T04:30:00Z")))
                .andExpect(jsonPath("$.slots[2].endTime", is("2026-07-10T05:15:00Z")));
    }

    @Test
    void anyStylistReturnsUnionDeduplicatedSlots() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 10); // Friday

        // Jessica working hours: Friday 09:00 - 11:00
        WorkingHoursEntity wh1 = new WorkingHoursEntity();
        wh1.setStaff(jessica);
        wh1.setDayOfWeek(DayOfWeek.FRIDAY);
        wh1.setStartTime(LocalTime.of(9, 0));
        wh1.setEndTime(LocalTime.of(11, 0));
        workingHoursRepository.saveAndFlush(wh1);

        // Kate working hours: Friday 10:00 - 12:00
        WorkingHoursEntity wh2 = new WorkingHoursEntity();
        wh2.setStaff(kate);
        wh2.setDayOfWeek(DayOfWeek.FRIDAY);
        wh2.setStartTime(LocalTime.of(10, 0));
        wh2.setEndTime(LocalTime.of(12, 0));
        workingHoursRepository.saveAndFlush(wh2);

        // Appointment for Jessica: 10:00 - 10:45 (blocks her 10:00 slot, but Kate is free)
        AppointmentEntity app = new AppointmentEntity();
        app.setCustomerName("Charlie");
        app.setCustomerEmail("charlie@example.com");
        app.setCustomerPhone("111");
        app.setService(haircut);
        app.setStaff(jessica);
        app.setStartTime(OffsetDateTime.of(2026, 7, 10, 10, 0, 0, 0, COLOMBO_OFFSET));
        app.setEndTime(OffsetDateTime.of(2026, 7, 10, 10, 45, 0, 0, COLOMBO_OFFSET));
        app.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.saveAndFlush(app);

        // Expected deduplicated Union slots (UTC):
        // 1. 09:00 - 09:45 -> 03:30 - 04:15 (Jessica free)
        // 2. 10:00 - 10:45 -> 04:30 - 05:15 (Kate free, Jessica blocked)
        // 3. 10:30 - 11:15 -> 05:00 - 05:45 (Kate free)
        // 4. 11:00 - 11:45 -> 05:30 - 06:15 (Kate free)
        // Jessica's 09:30 slot (09:30 - 10:15) overlaps Jessica's appointment 10:00 - 10:45 (since 10:00 < 10:15) -> excluded.

        mockMvc.perform(get("/api/booking/availability")
                        .param("serviceSlug", "haircut-45")
                        .param("staffSlug", "any")
                        .param("date", "2026-07-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceSlug", is("haircut-45")))
                .andExpect(jsonPath("$.staffSlug", is("any")))
                .andExpect(jsonPath("$.date", is("2026-07-10")))
                .andExpect(jsonPath("$.timeZone", is("Asia/Colombo")))
                .andExpect(jsonPath("$.slots", hasSize(4)))
                .andExpect(jsonPath("$.slots[0].startTime", is("2026-07-10T03:30:00Z")))
                .andExpect(jsonPath("$.slots[1].startTime", is("2026-07-10T04:30:00Z")))
                .andExpect(jsonPath("$.slots[2].startTime", is("2026-07-10T05:00:00Z")))
                .andExpect(jsonPath("$.slots[3].startTime", is("2026-07-10T05:30:00Z")));
    }

    @Test
    void validRequestWithNoAvailabilityReturnsEmptySlots() throws Exception {
        mockMvc.perform(get("/api/booking/availability")
                        .param("serviceSlug", "haircut-45")
                        .param("staffSlug", "jessica-stylist")
                        .param("date", "2026-07-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots", hasSize(0)));
    }

    @Test
    void missingParametersReturnBadRequest() throws Exception {
        // Missing serviceSlug
        mockMvc.perform(get("/api/booking/availability")
                        .param("staffSlug", "jessica-stylist")
                        .param("date", "2026-07-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Blank serviceSlug
        mockMvc.perform(get("/api/booking/availability")
                        .param("serviceSlug", " ")
                        .param("staffSlug", "jessica-stylist")
                        .param("date", "2026-07-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Malformed date
        mockMvc.perform(get("/api/booking/availability")
                        .param("serviceSlug", "haircut-45")
                        .param("staffSlug", "jessica-stylist")
                        .param("date", "2026/07/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}

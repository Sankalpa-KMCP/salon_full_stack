package com.velvetsalon.api.web;

import com.velvetsalon.api.domain.appointment.AppointmentEntity;
import com.velvetsalon.api.domain.appointment.AppointmentRepository;
import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import com.velvetsalon.api.domain.staff.WorkingHoursEntity;
import com.velvetsalon.api.domain.staff.WorkingHoursRepository;
import com.velvetsalon.api.web.dto.BookingRequest;
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
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class BookingControllerIntegrationTest {

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
    private AppointmentRepository appointmentRepository;

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");
    private StaffEntity stylistA;
    private ServiceEntity haircutService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        appointmentRepository.deleteAllInBatch();
        workingHoursRepository.deleteAllInBatch();
        staffRepository.deleteAllInBatch();
        serviceRepository.deleteAllInBatch();

        // 1. Create stylist
        StaffEntity staffA = new StaffEntity();
        staffA.setSlug("jessica-stylist");
        staffA.setName("Jessica Stylist");
        staffA.setRole("Senior Stylist");
        staffA.setIsActive(true);
        stylistA = staffRepository.saveAndFlush(staffA);

        // 2. Create service
        ServiceEntity service = new ServiceEntity();
        service.setSlug("haircut");
        service.setName("Haircut");
        service.setDurationMinutes(30);
        service.setPrice(BigDecimal.valueOf(45.00));
        service.setIsActive(true);
        haircutService = serviceRepository.saveAndFlush(service);

        // 3. Qualify stylist for service
        stylistA.getServices().add(haircutService);
        stylistA = staffRepository.saveAndFlush(stylistA);
    }

    @Test
    @Transactional
    void validNamedStylistBookingReturns201AndCorrectPayload() throws Exception {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(stylistA);
        wh.setDayOfWeek(dayOfWeek);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        String jsonRequest = String.format("""
                {
                    "serviceSlug": "haircut",
                    "staffSlug": "jessica-stylist",
                    "startTime": "%s",
                    "customerName": "John Doe",
                    "customerEmail": "john@example.com",
                    "customerPhone": "+123456",
                    "notes": "Some notes"
                }
                """, requestedStart.toString());

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.serviceSlug", is("haircut")))
                .andExpect(jsonPath("$.staffSlug", is("jessica-stylist")))
                .andExpect(jsonPath("$.staffName", is("Jessica Stylist")))
                .andExpect(jsonPath("$.startTime", is(requestedStart.toString())))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.cancellationToken", notNullValue()))
                .andExpect(jsonPath("$.notes").doesNotExist()); // Notes shouldn't be leaked
    }

    @Test
    @Transactional
    void validAnyStylistBookingReturns201AndResolvesStylist() throws Exception {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(stylistA);
        wh.setDayOfWeek(dayOfWeek);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        String jsonRequest = String.format("""
                {
                    "serviceSlug": "haircut",
                    "staffSlug": "any",
                    "startTime": "%s",
                    "customerName": "John Doe",
                    "customerEmail": "john@example.com",
                    "customerPhone": "+123456",
                    "notes": null
                }
                """, requestedStart.toString());

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.staffSlug", is("jessica-stylist"))) // Must resolve, not 'any'
                .andExpect(jsonPath("$.staffName", is("Jessica Stylist")));
    }

    @Test
    @Transactional
    void missingRequiredFieldsReturns400() throws Exception {
        String invalidJson = "{ \"serviceSlug\": \"haircut\" }"; // Missing everything else

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void invalidEmailReturns400() throws Exception {
        String jsonRequest = String.format("""
                {
                    "serviceSlug": "haircut",
                    "staffSlug": "jessica-stylist",
                    "startTime": "%s",
                    "customerName": "John Doe",
                    "customerEmail": "invalid-email-format",
                    "customerPhone": "+123456",
                    "notes": null
                }
                """, Instant.now().toString());

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void bookingServiceValidationFailureReturns400() throws Exception {
        Instant requestedStart = Instant.now().minusSeconds(86400 * 10); // Past date to trigger horizon failure

        String jsonRequest = String.format("""
                {
                    "serviceSlug": "haircut",
                    "staffSlug": "jessica-stylist",
                    "startTime": "%s",
                    "customerName": "John Doe",
                    "customerEmail": "john@example.com",
                    "customerPhone": "+123456",
                    "notes": null
                }
                """, requestedStart.toString());

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        assertEquals(0, appointmentRepository.count(), "No appointment should be persisted on 400");
    }

    @Test
    @Transactional
    void differentlyCasedAnyDoesNotTriggerAnyStylistAllocation() throws Exception {
        LocalDate date = LocalDate.now(COLOMBO_ZONE).plusDays(2);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        WorkingHoursEntity wh = new WorkingHoursEntity();
        wh.setStaff(stylistA);
        wh.setDayOfWeek(dayOfWeek);
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(17, 0));
        workingHoursRepository.saveAndFlush(wh);

        Instant requestedStart = date.atTime(10, 0).atZone(COLOMBO_ZONE).toInstant();

        // Exact uppercase "ANY", service accepts exact lowercase "any"
        String jsonRequest = String.format("""
                {
                    "serviceSlug": "haircut",
                    "staffSlug": "ANY",
                    "startTime": "%s",
                    "customerName": "John Doe",
                    "customerEmail": "john@example.com",
                    "customerPhone": "+123456",
                    "notes": null
                }
                """, requestedStart.toString());

        // Expect 400 Bad Request because "ANY" will be treated as a named stylist slug which does not exist
        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        // 2. Whitespace variant " any "
        String jsonRequest2 = String.format("""
                {
                    "serviceSlug": "haircut",
                    "staffSlug": " any ",
                    "startTime": "%s",
                    "customerName": "John Doe",
                    "customerEmail": "john@example.com",
                    "customerPhone": "+123456",
                    "notes": null
                }
                """, requestedStart.toString());

        mockMvc.perform(post("/api/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest2))
                .andExpect(status().isBadRequest());
    }
}

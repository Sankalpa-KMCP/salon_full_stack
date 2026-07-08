package com.velvetsalon.api.web;

import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class CatalogControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private StaffRepository staffRepository;

    private ServiceEntity activeService;
    private ServiceEntity inactiveService;
    private StaffEntity activeStaff;
    private StaffEntity inactiveStaff;
    private StaffEntity unqualifiedStaff;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        staffRepository.deleteAllInBatch();
        serviceRepository.deleteAllInBatch();

        // Services
        ServiceEntity s1 = new ServiceEntity();
        s1.setSlug("haircut");
        s1.setName("Haircut");
        s1.setDurationMinutes(30);
        s1.setPrice(BigDecimal.valueOf(45.50));
        s1.setIsActive(true);
        activeService = serviceRepository.saveAndFlush(s1);

        ServiceEntity s2 = new ServiceEntity();
        s2.setSlug("facial");
        s2.setName("Facial");
        s2.setDurationMinutes(60);
        s2.setPrice(BigDecimal.valueOf(80.00));
        s2.setIsActive(false); // Inactive
        inactiveService = serviceRepository.saveAndFlush(s2);

        // Staff
        StaffEntity st1 = new StaffEntity();
        st1.setSlug("emma");
        st1.setName("Emma");
        st1.setRole("Senior Stylist");
        st1.setIsActive(true);
        st1.getServices().add(activeService);
        activeStaff = staffRepository.saveAndFlush(st1);

        StaffEntity st2 = new StaffEntity();
        st2.setSlug("john");
        st2.setName("John");
        st2.setRole("Junior Stylist");
        st2.setIsActive(false); // Inactive
        st2.getServices().add(activeService);
        inactiveStaff = staffRepository.saveAndFlush(st2);

        StaffEntity st3 = new StaffEntity();
        st3.setSlug("sarah");
        st3.setName("Sarah");
        st3.setRole("Colorist");
        st3.setIsActive(true);
        // Not qualified for haircut
        unqualifiedStaff = staffRepository.saveAndFlush(st3);
    }

    @Test
    @Transactional
    void getServicesReturnsOnlyActiveAndSafeFields() throws Exception {
        mockMvc.perform(get("/api/catalog/services")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].slug", is("haircut")))
                .andExpect(jsonPath("$[0].name", is("Haircut")))
                .andExpect(jsonPath("$[0].price", is(45.50))) // raw decimal
                .andExpect(jsonPath("$[0].durationMinutes", is(30)))
                .andExpect(jsonPath("$[0].id").doesNotExist()) // UUID hidden
                .andExpect(jsonPath("$[0].isActive").doesNotExist()); // Internal flag hidden
    }

    @Test
    @Transactional
    void getStaffReturnsOnlyActiveAndSafeFields() throws Exception {
        mockMvc.perform(get("/api/catalog/staff")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Emma and Sarah
                // Because ordered by name ASC: Emma comes first, Sarah second
                .andExpect(jsonPath("$[0].slug", is("emma")))
                .andExpect(jsonPath("$[0].name", is("Emma")))
                .andExpect(jsonPath("$[1].slug", is("sarah")))
                .andExpect(jsonPath("$[1].name", is("Sarah")))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[0].isActive").doesNotExist())
                .andExpect(jsonPath("$[0].services").doesNotExist()); // No entity graph leakage
    }

    @Test
    @Transactional
    void getStaffFilteredByServiceReturnsOnlyQualifiedActiveStaff() throws Exception {
        mockMvc.perform(get("/api/catalog/staff?serviceSlug=haircut")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Only Emma (John is inactive, Sarah is unqualified)
                .andExpect(jsonPath("$[0].slug", is("emma")));
    }

    @Test
    @Transactional
    void getStaffFilteredByUnknownServiceReturns404() throws Exception {
        mockMvc.perform(get("/api/catalog/staff?serviceSlug=unknown-service")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getStaffFilteredByInactiveServiceReturns404() throws Exception {
        mockMvc.perform(get("/api/catalog/staff?serviceSlug=facial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getEmptyCatalogReturnsEmptyArray() throws Exception {
        staffRepository.deleteAllInBatch();
        serviceRepository.deleteAllInBatch();

        mockMvc.perform(get("/api/catalog/services")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/catalog/staff")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

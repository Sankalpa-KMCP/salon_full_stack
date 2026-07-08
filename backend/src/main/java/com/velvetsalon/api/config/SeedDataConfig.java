package com.velvetsalon.api.config;

import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import com.velvetsalon.api.domain.staff.WorkingHoursEntity;
import com.velvetsalon.api.domain.staff.WorkingHoursRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);

    @Bean
    @Transactional
    public CommandLineRunner seedDatabase(
            ServiceRepository serviceRepository,
            StaffRepository staffRepository,
            WorkingHoursRepository workingHoursRepository) {

        return args -> {
            log.info("Seed data is enabled. Checking if seed is required...");
            if (serviceRepository.count() > 0 || staffRepository.count() > 0) {
                log.info("Database already contains services or staff. Skipping seed.");
                return;
            }

            log.info("Seeding services...");
            ServiceEntity haircut = new ServiceEntity();
            haircut.setSlug("classic-haircut");
            haircut.setName("Classic Haircut");
            haircut.setDescription("A traditional, precise haircut tailored to your style.");
            haircut.setPrice(new BigDecimal("45.00"));
            haircut.setDurationMinutes(60);
            haircut.setIsActive(true);

            ServiceEntity coloring = new ServiceEntity();
            coloring.setSlug("full-color");
            coloring.setName("Full Color");
            coloring.setDescription("Professional full head color application.");
            coloring.setPrice(new BigDecimal("120.00"));
            coloring.setDurationMinutes(120);
            coloring.setIsActive(true);

            serviceRepository.saveAll(List.of(haircut, coloring));

            log.info("Seeding staff...");
            StaffEntity emma = new StaffEntity();
            emma.setSlug("emma-stylist");
            emma.setName("Emma");
            emma.setRole("Senior Stylist");
            emma.setBio("Emma specializes in precision cuts and modern coloring.");
            emma.setIsActive(true);
            emma.getServices().add(haircut);
            emma.getServices().add(coloring);

            StaffEntity liam = new StaffEntity();
            liam.setSlug("liam-barber");
            liam.setName("Liam");
            liam.setRole("Barber");
            liam.setBio("Liam is our resident expert in classic cuts.");
            liam.setIsActive(true);
            liam.getServices().add(haircut);

            staffRepository.saveAll(List.of(emma, liam));

            log.info("Seeding working hours...");
            seedWorkingHours(workingHoursRepository, emma);
            seedWorkingHours(workingHoursRepository, liam);

            log.info("Seed data insertion complete.");
        };
    }

    private void seedWorkingHours(WorkingHoursRepository repo, StaffEntity staff) {
        // Monday to Friday, 9:00 AM to 5:00 PM (17:00)
        List<DayOfWeek> workDays = List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        );

        for (DayOfWeek day : workDays) {
            WorkingHoursEntity wh = new WorkingHoursEntity();
            wh.setStaff(staff);
            wh.setDayOfWeek(day);
            wh.setStartTime(LocalTime.of(9, 0));
            wh.setEndTime(LocalTime.of(17, 0));
            repo.save(wh);
        }
    }
}

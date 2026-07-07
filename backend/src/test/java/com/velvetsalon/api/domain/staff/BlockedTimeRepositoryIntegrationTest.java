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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "POSTGRES_DB=testdb",
        "POSTGRES_USER=testuser",
        "POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class BlockedTimeRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BlockedTimeRepository blockedTimeRepository;

    @Test
    @Transactional
    void canSaveAndRetrieveBlockedTime() {
        // 1. Create and save a Staff member
        StaffEntity staff = new StaffEntity();
        staff.setSlug("kate-stylist");
        staff.setName("Kate Stylist");
        staff.setRole("Stylist");
        staff = staffRepository.save(staff);

        // 2. Create and save a Blocked Time record
        OffsetDateTime startTime = OffsetDateTime.of(2026, 7, 10, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endTime = OffsetDateTime.of(2026, 7, 10, 11, 0, 0, 0, ZoneOffset.UTC);

        BlockedTimeEntity blockedTime = new BlockedTimeEntity();
        blockedTime.setStaff(staff);
        blockedTime.setStartTime(startTime);
        blockedTime.setEndTime(endTime);
        blockedTime.setReason("Lunch Break");

        BlockedTimeEntity saved = blockedTimeRepository.save(blockedTime);
        assertNotNull(saved.getId());

        // 3. Retrieve and verify
        Optional<BlockedTimeEntity> retrievedOpt = blockedTimeRepository.findById(saved.getId());
        assertTrue(retrievedOpt.isPresent());
        BlockedTimeEntity retrieved = retrievedOpt.get();
        assertEquals("Lunch Break", retrieved.getReason());
        assertTrue(startTime.isEqual(retrieved.getStartTime()));
        assertTrue(endTime.isEqual(retrieved.getEndTime()));
    }

    @Test
    @Transactional
    void verifiesHalfOpenIntervalOverlapLogic() {
        // 1. Create and save a Staff member
        StaffEntity staff = new StaffEntity();
        staff.setSlug("mark-stylist");
        staff.setName("Mark Stylist");
        staff.setRole("Stylist");
        staff = staffRepository.save(staff);

        // Blocked time: [10:00, 11:00) UTC
        OffsetDateTime blockStart = OffsetDateTime.of(2026, 7, 10, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime blockEnd = OffsetDateTime.of(2026, 7, 10, 11, 0, 0, 0, ZoneOffset.UTC);

        BlockedTimeEntity block = new BlockedTimeEntity();
        block.setStaff(staff);
        block.setStartTime(blockStart);
        block.setEndTime(blockEnd);
        block.setReason("Training");
        blockedTimeRepository.save(block);

        // Test Overlaps:
        // A. Requested overlaps start: [09:30, 10:30)
        List<BlockedTimeEntity> overlapA = blockedTimeRepository.findOverlappingBlockedTimes(
                staff.getId(),
                OffsetDateTime.of(2026, 7, 10, 9, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, ZoneOffset.UTC)
        );
        assertEquals(1, overlapA.size());

        // B. Requested overlaps end: [10:30, 11:30)
        List<BlockedTimeEntity> overlapB = blockedTimeRepository.findOverlappingBlockedTimes(
                staff.getId(),
                OffsetDateTime.of(2026, 7, 10, 10, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 7, 10, 11, 30, 0, 0, ZoneOffset.UTC)
        );
        assertEquals(1, overlapB.size());

        // C. Requested overlaps inside: [10:15, 10:45)
        List<BlockedTimeEntity> overlapC = blockedTimeRepository.findOverlappingBlockedTimes(
                staff.getId(),
                OffsetDateTime.of(2026, 7, 10, 10, 15, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 7, 10, 10, 45, 0, 0, ZoneOffset.UTC)
        );
        assertEquals(1, overlapC.size());

        // D. Adjacent before (no overlap): [09:00, 10:00)
        List<BlockedTimeEntity> overlapD = blockedTimeRepository.findOverlappingBlockedTimes(
                staff.getId(),
                OffsetDateTime.of(2026, 7, 10, 9, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 7, 10, 10, 0, 0, 0, ZoneOffset.UTC)
        );
        assertEquals(0, overlapD.size());

        // E. Adjacent after (no overlap): [11:00, 12:00)
        List<BlockedTimeEntity> overlapE = blockedTimeRepository.findOverlappingBlockedTimes(
                staff.getId(),
                OffsetDateTime.of(2026, 7, 10, 11, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 7, 10, 12, 0, 0, 0, ZoneOffset.UTC)
        );
        assertEquals(0, overlapE.size());
    }

    @Test
    @Transactional
    void cannotSaveInvalidBlockedTimeEndTimeBeforeStartTime() {
        // 1. Create and save a Staff member
        StaffEntity staff = new StaffEntity();
        staff.setSlug("tim-stylist");
        staff.setName("Tim Stylist");
        staff.setRole("Stylist");
        staff = staffRepository.save(staff);

        // 2. Try saving blocked time where end_time < start_time
        BlockedTimeEntity invalidBlock = new BlockedTimeEntity();
        invalidBlock.setStaff(staff);
        invalidBlock.setStartTime(OffsetDateTime.of(2026, 7, 10, 12, 0, 0, 0, ZoneOffset.UTC));
        invalidBlock.setEndTime(OffsetDateTime.of(2026, 7, 10, 11, 0, 0, 0, ZoneOffset.UTC)); // Invalid
        invalidBlock.setReason("Invalid time range");

        assertThrows(DataIntegrityViolationException.class, () -> {
            blockedTimeRepository.saveAndFlush(invalidBlock);
        });
    }
}

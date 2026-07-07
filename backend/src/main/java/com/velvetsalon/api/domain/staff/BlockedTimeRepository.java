package com.velvetsalon.api.domain.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BlockedTimeRepository extends JpaRepository<BlockedTimeEntity, UUID> {

    @Query("SELECT b FROM BlockedTimeEntity b WHERE b.staff.id = :staffId " +
           "AND b.startTime < :requestedEnd AND b.endTime > :requestedStart")
    List<BlockedTimeEntity> findOverlappingBlockedTimes(
            @Param("staffId") UUID staffId,
            @Param("requestedStart") OffsetDateTime requestedStart,
            @Param("requestedEnd") OffsetDateTime requestedEnd
    );
}

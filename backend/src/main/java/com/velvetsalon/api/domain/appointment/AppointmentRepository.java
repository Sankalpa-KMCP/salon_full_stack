package com.velvetsalon.api.domain.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    Optional<AppointmentEntity> findByCancellationToken(UUID cancellationToken);

    @Query("SELECT a FROM AppointmentEntity a WHERE a.staff.id = :staffId " +
           "AND a.status IN :statuses " +
           "AND a.startTime < :requestedEnd AND a.endTime > :requestedStart")
    List<AppointmentEntity> findOverlappingActiveAppointments(
            @Param("staffId") UUID staffId,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("requestedStart") OffsetDateTime requestedStart,
            @Param("requestedEnd") OffsetDateTime requestedEnd
    );
}

package com.velvetsalon.api.domain.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHoursEntity, UUID> {
    List<WorkingHoursEntity> findByStaffIdOrderByDayOfWeekAscStartTimeAsc(UUID staffId);
}

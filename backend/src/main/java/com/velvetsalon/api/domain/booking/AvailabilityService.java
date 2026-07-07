package com.velvetsalon.api.domain.booking;

import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import com.velvetsalon.api.domain.staff.WorkingHoursEntity;
import com.velvetsalon.api.domain.staff.WorkingHoursRepository;
import com.velvetsalon.api.domain.staff.BlockedTimeEntity;
import com.velvetsalon.api.domain.staff.BlockedTimeRepository;
import com.velvetsalon.api.domain.appointment.AppointmentEntity;
import com.velvetsalon.api.domain.appointment.AppointmentRepository;
import com.velvetsalon.api.domain.appointment.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    private final ServiceRepository serviceRepository;
    private final StaffRepository staffRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final BlockedTimeRepository blockedTimeRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<AvailableSlot> getAvailableSlots(String serviceSlug, String staffSlug, LocalDate date) {
        Optional<ServiceEntity> serviceOpt = serviceRepository.findBySlug(serviceSlug);
        Optional<StaffEntity> staffOpt = staffRepository.findBySlug(staffSlug);

        if (serviceOpt.isEmpty() || staffOpt.isEmpty()) {
            return Collections.emptyList();
        }

        ServiceEntity service = serviceOpt.get();
        StaffEntity staff = staffOpt.get();

        if (!service.getIsActive() || !staff.getIsActive()) {
            return Collections.emptyList();
        }

        // Verify qualification (must be qualified for this service)
        boolean isQualified = staff.getServices().stream()
                .anyMatch(s -> s.getId().equals(service.getId()));
        if (!isQualified) {
            return Collections.emptyList();
        }

        return calculateStaffAvailabilityForService(service, staff, date).stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailableSlot> getAnyStylistAvailableSlots(String serviceSlug, LocalDate date) {
        Optional<ServiceEntity> serviceOpt = serviceRepository.findBySlug(serviceSlug);
        if (serviceOpt.isEmpty()) {
            return Collections.emptyList();
        }

        ServiceEntity service = serviceOpt.get();
        if (!service.getIsActive()) {
            return Collections.emptyList();
        }

        // Find active staff qualified for this service
        List<StaffEntity> activeQualifiedStaff = staffRepository.findActiveStaffByQualifiedService(service.getId());

        List<AvailableSlot> unionSlots = new ArrayList<>();
        for (StaffEntity staff : activeQualifiedStaff) {
            unionSlots.addAll(calculateStaffAvailabilityForService(service, staff, date));
        }

        return unionSlots.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<AvailableSlot> calculateStaffAvailabilityForService(
            ServiceEntity service, StaffEntity staff, LocalDate date) {

        // Define target day time boundaries in Colombo zone to query overlaps
        ZonedDateTime zdtStart = date.atStartOfDay(COLOMBO_ZONE);
        ZonedDateTime zdtEnd = date.plusDays(1).atStartOfDay(COLOMBO_ZONE);
        OffsetDateTime startOfDay = zdtStart.toOffsetDateTime();
        OffsetDateTime endOfDay = zdtEnd.toOffsetDateTime();

        // Get working hours for the staff member on the requested day of week
        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<WorkingHoursEntity> workingHoursList = workingHoursRepository.findByStaffIdAndDayOfWeek(staff.getId(), dayOfWeek);

        if (workingHoursList.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch blocked times and active appointments overlapping the local day window
        List<BlockedTimeEntity> blockedTimes = blockedTimeRepository.findOverlappingBlockedTimes(
                staff.getId(), startOfDay, endOfDay);
        List<AppointmentEntity> activeAppointments = appointmentRepository.findOverlappingActiveAppointments(
                staff.getId(),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED),
                startOfDay,
                endOfDay
        );

        List<AvailableSlot> candidateSlots = new ArrayList<>();
        int serviceDuration = service.getDurationMinutes();

        for (WorkingHoursEntity wh : workingHoursList) {
            OffsetDateTime whStart = date.atTime(wh.getStartTime()).atZone(COLOMBO_ZONE).toOffsetDateTime();
            OffsetDateTime whEnd = date.atTime(wh.getEndTime()).atZone(COLOMBO_ZONE).toOffsetDateTime();

            OffsetDateTime candidateStart = whStart;
            while (true) {
                OffsetDateTime candidateEnd = candidateStart.plusMinutes(serviceDuration);
                if (candidateEnd.isAfter(whEnd)) {
                    break;
                }

                candidateSlots.add(new AvailableSlot(candidateStart, candidateEnd));
                candidateStart = candidateStart.plusMinutes(30);
            }
        }

        // Filter slots against blocked times and active appointments
        return candidateSlots.stream()
                .filter(slot -> {
                    // Check blocked times overlap
                    boolean overlapsBlocked = blockedTimes.stream().anyMatch(bt ->
                            bt.getStartTime().isBefore(slot.endTime()) && bt.getEndTime().isAfter(slot.startTime()));
                    if (overlapsBlocked) {
                        return false;
                    }

                    // Check active appointments overlap
                    boolean overlapsApp = activeAppointments.stream().anyMatch(app ->
                            app.getStartTime().isBefore(slot.endTime()) && app.getEndTime().isAfter(slot.startTime()));
                    return !overlapsApp;
                })
                .collect(Collectors.toList());
    }
}

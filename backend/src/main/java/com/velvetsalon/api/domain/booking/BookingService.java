package com.velvetsalon.api.domain.booking;

import com.velvetsalon.api.domain.appointment.AppointmentEntity;
import com.velvetsalon.api.domain.appointment.AppointmentRepository;
import com.velvetsalon.api.domain.appointment.AppointmentStatus;
import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final ZoneId COLOMBO_ZONE = ZoneId.of("Asia/Colombo");

    private final ServiceRepository serviceRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityService availabilityService;

    @Transactional
    public AppointmentEntity createAppointment(
            String serviceSlug,
            String staffSlug,
            Instant requestedStart,
            String customerName,
            String customerEmail,
            String customerPhone,
            String notes
    ) {
        if (serviceSlug == null || serviceSlug.isBlank()) {
            throw new BookingValidationException("Service slug must not be blank");
        }
        if (staffSlug == null || staffSlug.isBlank()) {
            throw new BookingValidationException("Staff slug must not be blank");
        }
        if (requestedStart == null) {
            throw new BookingValidationException("Requested start time must not be null");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new BookingValidationException("Customer name must not be blank");
        }
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new BookingValidationException("Customer email must not be blank");
        }
        if (customerPhone == null || customerPhone.isBlank()) {
            throw new BookingValidationException("Customer phone must not be blank");
        }

        // 1. Service Verification
        ServiceEntity service = serviceRepository.findBySlug(serviceSlug)
                .orElseThrow(() -> new BookingValidationException("Service not found: " + serviceSlug));
        if (!Boolean.TRUE.equals(service.getIsActive())) {
            throw new BookingValidationException("Service is inactive: " + serviceSlug);
        }

        // 2. Booking Horizon boundary checks
        LocalDate requestedDate = requestedStart.atZone(COLOMBO_ZONE).toLocalDate();
        LocalDate todayColombo = LocalDate.now(COLOMBO_ZONE);
        LocalDate maxDateColombo = todayColombo.plusDays(30);

        if (requestedDate.isBefore(todayColombo) || requestedDate.isAfter(maxDateColombo)) {
            throw new BookingValidationException("Booking date must be between today and 30 days from now (inclusive) in local time");
        }

        StaffEntity selectedStaff = null;
        if ("any".equals(staffSlug)) {
            // Find active staff qualified for the service
            List<StaffEntity> activeQualifiedStaff = staffRepository.findActiveStaffByQualifiedService(service.getId());
            // Make a mutable copy to sort safely
            List<StaffEntity> sortedStaff = new ArrayList<>(activeQualifiedStaff);
            sortedStaff.sort(Comparator.comparing(StaffEntity::getSlug));

            for (StaffEntity staff : sortedStaff) {
                List<AvailableSlot> slots = availabilityService.getAvailableSlots(serviceSlug, staff.getSlug(), requestedDate);
                boolean hasSlot = slots.stream().anyMatch(slot -> slot.startTime().toInstant().equals(requestedStart));
                if (hasSlot) {
                    selectedStaff = staff;
                    break;
                }
            }

            if (selectedStaff == null) {
                throw new BookingValidationException("No qualified stylists are available at the requested time");
            }
        } else {
            // Find named stylist
            StaffEntity staff = staffRepository.findBySlug(staffSlug)
                    .orElseThrow(() -> new BookingValidationException("Stylist not found: " + staffSlug));
            if (!Boolean.TRUE.equals(staff.getIsActive())) {
                throw new BookingValidationException("Stylist is inactive: " + staffSlug);
            }

            // Check qualification (staff must be qualified for the service)
            boolean isQualified = staff.getServices().stream().anyMatch(s -> s.getId().equals(service.getId()));
            if (!isQualified) {
                throw new BookingValidationException("Stylist is not qualified for this service");
            }

            // Check availability
            List<AvailableSlot> slots = availabilityService.getAvailableSlots(serviceSlug, staffSlug, requestedDate);
            boolean hasSlot = slots.stream().anyMatch(slot -> slot.startTime().toInstant().equals(requestedStart));
            if (!hasSlot) {
                throw new BookingValidationException("Stylist is not available at the requested time");
            }
            selectedStaff = staff;
        }

        // 3. Build & Save Appointment
        OffsetDateTime startTime = requestedStart.atZone(COLOMBO_ZONE).toOffsetDateTime();
        OffsetDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        AppointmentEntity app = new AppointmentEntity();
        app.setCustomerName(customerName);
        app.setCustomerEmail(customerEmail);
        app.setCustomerPhone(customerPhone);
        app.setNotes(notes);
        app.setService(service);
        app.setStaff(selectedStaff);
        app.setStartTime(startTime);
        app.setEndTime(endTime);
        app.setStatus(AppointmentStatus.CONFIRMED);

        try {
            return appointmentRepository.saveAndFlush(app);
        } catch (org.springframework.dao.DataIntegrityViolationException | org.springframework.dao.CannotAcquireLockException ex) {
            String msg = ex.getMostSpecificCause().getMessage();
            if (msg != null && (msg.contains("no_double_booking") || msg.contains("23P01") || msg.contains("deadlock detected") || msg.contains("40P01"))) {
                throw new DoubleBookingException("The requested slot is already booked for this stylist");
            }
            if (msg != null && (msg.contains("fk_appointments_staff_service") || msg.contains("23503"))) {
                throw new UnqualifiedStylistException("Stylist is not qualified for the requested service");
            }
            throw ex;
        }
    }
}

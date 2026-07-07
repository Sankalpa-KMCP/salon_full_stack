package com.velvetsalon.api.web;

import com.velvetsalon.api.domain.booking.AvailabilityService;
import com.velvetsalon.api.domain.booking.AvailableSlot;
import com.velvetsalon.api.web.dto.AvailabilityResponse;
import com.velvetsalon.api.web.dto.TimeSlotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/availability")
    public AvailabilityResponse getAvailability(
            @RequestParam String serviceSlug,
            @RequestParam String staffSlug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (serviceSlug == null || serviceSlug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "serviceSlug must not be blank");
        }
        if (staffSlug == null || staffSlug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "staffSlug must not be blank");
        }
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must not be null");
        }

        List<AvailableSlot> slots;
        if ("any".equalsIgnoreCase(staffSlug.trim())) {
            slots = availabilityService.getAnyStylistAvailableSlots(serviceSlug, date);
        } else {
            slots = availabilityService.getAvailableSlots(serviceSlug, staffSlug, date);
        }

        List<TimeSlotDto> slotDtos = slots.stream()
                .map(s -> new TimeSlotDto(s.startTime().toInstant(), s.endTime().toInstant()))
                .collect(Collectors.toList());

        return new AvailabilityResponse(
                serviceSlug,
                staffSlug,
                date,
                "Asia/Colombo",
                slotDtos
        );
    }
}

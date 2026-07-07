package com.velvetsalon.api.web.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(
        String serviceSlug,
        String staffSlug,
        LocalDate date,
        String timeZone,
        List<TimeSlotDto> slots
) {
}

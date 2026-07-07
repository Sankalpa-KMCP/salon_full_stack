package com.velvetsalon.api.web.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        String serviceSlug,
        String staffSlug,
        String staffName,
        Instant startTime,
        Instant endTime,
        String status,
        UUID cancellationToken
) {
}

package com.velvetsalon.api.web.dto;

import java.time.Instant;

public record TimeSlotDto(Instant startTime, Instant endTime) {
}

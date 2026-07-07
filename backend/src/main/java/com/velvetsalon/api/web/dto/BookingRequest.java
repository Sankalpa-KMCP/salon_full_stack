package com.velvetsalon.api.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record BookingRequest(
        @NotBlank String serviceSlug,
        @NotBlank String staffSlug,
        @NotNull Instant startTime,
        @NotBlank String customerName,
        @NotBlank @Email String customerEmail,
        @NotBlank String customerPhone,
        String notes
) {
}

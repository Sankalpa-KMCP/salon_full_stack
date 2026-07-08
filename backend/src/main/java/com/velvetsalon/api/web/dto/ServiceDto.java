package com.velvetsalon.api.web.dto;

import java.math.BigDecimal;

public record ServiceDto(
        String slug,
        String name,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        String imageUrl
) {
}

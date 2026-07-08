package com.velvetsalon.api.web.dto;

public record StaffDto(
        String slug,
        String name,
        String role,
        String specialty,
        String bio,
        String imageUrl
) {
}

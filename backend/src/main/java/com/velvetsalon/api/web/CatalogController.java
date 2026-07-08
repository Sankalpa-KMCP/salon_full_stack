package com.velvetsalon.api.web;

import com.velvetsalon.api.domain.service.ServiceEntity;
import com.velvetsalon.api.domain.service.ServiceRepository;
import com.velvetsalon.api.domain.staff.StaffEntity;
import com.velvetsalon.api.domain.staff.StaffRepository;
import com.velvetsalon.api.web.dto.ServiceDto;
import com.velvetsalon.api.web.dto.StaffDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ServiceRepository serviceRepository;
    private final StaffRepository staffRepository;

    @GetMapping("/services")
    public List<ServiceDto> getActiveServices() {
        return serviceRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toServiceDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/staff")
    public List<StaffDto> getActiveStaff(@RequestParam(required = false) String serviceSlug) {
        if (serviceSlug == null || serviceSlug.isBlank()) {
            return staffRepository.findByIsActiveTrueOrderByNameAsc()
                    .stream()
                    .map(this::toStaffDto)
                    .collect(Collectors.toList());
        }

        ServiceEntity service = serviceRepository.findBySlugAndIsActiveTrue(serviceSlug.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found or inactive"));

        return staffRepository.findActiveStaffByQualifiedService(service.getId())
                .stream()
                .map(this::toStaffDto)
                .collect(Collectors.toList());
    }

    private ServiceDto toServiceDto(ServiceEntity entity) {
        return new ServiceDto(
                entity.getSlug(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getDurationMinutes(),
                entity.getImageUrl()
        );
    }

    private StaffDto toStaffDto(StaffEntity entity) {
        return new StaffDto(
                entity.getSlug(),
                entity.getName(),
                entity.getRole(),
                entity.getSpecialty(),
                entity.getBio(),
                entity.getImageUrl()
        );
    }
}

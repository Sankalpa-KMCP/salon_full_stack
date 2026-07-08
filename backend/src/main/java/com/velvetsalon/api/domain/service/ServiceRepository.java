package com.velvetsalon.api.domain.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    Optional<ServiceEntity> findBySlug(String slug);
    Optional<ServiceEntity> findBySlugAndIsActiveTrue(String slug);
    List<ServiceEntity> findByIsActiveTrue();
    List<ServiceEntity> findByIsActiveTrueOrderByNameAsc();
}

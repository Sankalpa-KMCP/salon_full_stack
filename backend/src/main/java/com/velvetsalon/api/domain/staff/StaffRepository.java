package com.velvetsalon.api.domain.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, UUID> {
    Optional<StaffEntity> findBySlug(String slug);
    List<StaffEntity> findByIsActiveTrue();
}

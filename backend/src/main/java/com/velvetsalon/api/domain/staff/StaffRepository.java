package com.velvetsalon.api.domain.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, UUID> {
    Optional<StaffEntity> findBySlug(String slug);
    List<StaffEntity> findByIsActiveTrue();
    List<StaffEntity> findByIsActiveTrueOrderByNameAsc();

    @Query("SELECT s FROM StaffEntity s JOIN s.services serv WHERE s.isActive = true AND serv.id = :serviceId ORDER BY s.name ASC")
    List<StaffEntity> findActiveStaffByQualifiedService(@Param("serviceId") UUID serviceId);
}

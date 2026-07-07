ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_staff_service
    FOREIGN KEY (staff_id, service_id)
    REFERENCES staff_services (staff_id, service_id);

package com.velvetsalon.api.web;

import com.velvetsalon.api.domain.appointment.AppointmentEntity;
import com.velvetsalon.api.domain.booking.BookingService;
import com.velvetsalon.api.domain.booking.BookingValidationException;
import com.velvetsalon.api.domain.booking.DoubleBookingException;
import com.velvetsalon.api.domain.booking.UnqualifiedStylistException;
import com.velvetsalon.api.web.dto.BookingRequest;
import com.velvetsalon.api.web.dto.BookingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        try {
            AppointmentEntity appointment = bookingService.createAppointment(
                    request.serviceSlug(),
                    request.staffSlug(),
                    request.startTime(),
                    request.customerName(),
                    request.customerEmail(),
                    request.customerPhone(),
                    request.notes()
            );

            return new BookingResponse(
                    appointment.getId(),
                    appointment.getService().getSlug(),
                    appointment.getStaff().getSlug(),
                    appointment.getStaff().getName(),
                    appointment.getStartTime().toInstant(),
                    appointment.getEndTime().toInstant(),
                    appointment.getStatus().name(),
                    appointment.getCancellationToken()
                );
        } catch (BookingValidationException | UnqualifiedStylistException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (DoubleBookingException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }
}

package com.dineflow.api.controller;

import com.dineflow.application.service.ReservationService;
import com.dineflow.domain.entity.Reservation;
import com.dineflow.domain.enums.ReservationStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation createReservation(@RequestBody ReservationRequest request) {

        return reservationService.createReservation(
                request.tenantId(),
                request.restaurantId(),
                request.tableId(),
                request.customerId(),
                request.startTime(),
                request.endTime(),
                request.status()
        );
    }

    public record ReservationRequest(
            Long tenantId,
            Long restaurantId,
            Long tableId,
            Long customerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status
    ) {}
}
package com.dineflow.application.port;

import com.dineflow.domain.entity.Reservation;
import com.dineflow.domain.enums.ReservationStatus;

import java.time.LocalDateTime;

public interface ReservationRepositoryPort {

    Reservation save(Reservation reservation);

    boolean existsOverlappingReservation(
            Long tenantId,
            Long restaurantId,
            Long tableId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status
    );

}

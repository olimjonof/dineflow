package com.dineflow.infrastructure.persistence;

import com.dineflow.application.port.ReservationRepositoryPort;
import com.dineflow.domain.entity.Reservation;
import com.dineflow.domain.enums.ReservationStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
@Primary
@Repository
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    @Override
    public Reservation save(Reservation reservation) {
        return null;
    }

    @Override
    public boolean existsOverlappingReservation(
            Long tenantId,
            Long restaurantId,
            Long tableId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status
    ) {
        return false;
    }
}
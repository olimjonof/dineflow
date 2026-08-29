package com.dineflow.infrastructure.persistence;

import com.dineflow.domain.entity.Reservation;
import com.dineflow.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTenantIdAndRestaurantIdAndTableIdAndStatus(
            Long tenantId,
            Long restaurantId,
            Long tableId,
            ReservationStatus status
    );
}
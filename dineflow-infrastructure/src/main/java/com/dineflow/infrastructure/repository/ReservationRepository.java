package com.dineflow.infrastructure.repository;

import com.dineflow.application.port.ReservationRepositoryPort;
import com.dineflow.domain.entity.Reservation;
import com.dineflow.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryPort {

    @Query("""
    SELECT COUNT(r) > 0
    FROM Reservation r
    WHERE r.tenantId = :tenantId
      AND r.restaurantId = :restaurantId
      AND r.tableId = :tableId
      AND r.status = :status
      AND r.bookingStartTime < :endTime
      AND r.bookingEndTime > :startTime
""")
    boolean existsOverlappingReservation(
            Long tenantId,
            Long restaurantId,
            Long tableId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status
    );
}

package com.dineflow.application.service;

import com.dineflow.application.dto.ReservationCreatedEvent;
import com.dineflow.application.exception.ConflictException;
import com.dineflow.application.exception.NotFoundException;
import com.dineflow.application.port.EventPayloadSerializer;
import com.dineflow.application.port.DiningTableRepositoryPort;
import com.dineflow.application.port.OutboxRepositoryPort;
import com.dineflow.application.port.ReservationRepositoryPort;
import com.dineflow.application.port.RestaurantRepositoryPort;
import com.dineflow.domain.entity.OutboxEvent;
import com.dineflow.domain.entity.Reservation;
import com.dineflow.domain.enums.OutboxStatus;
import com.dineflow.domain.enums.ReservationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReservationService {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final OutboxRepositoryPort outboxRepositoryPort;
    private final EventPayloadSerializer eventPayloadSerializer;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final DiningTableRepositoryPort diningTableRepositoryPort;

    public ReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            OutboxRepositoryPort outboxRepositoryPort,
            EventPayloadSerializer eventPayloadSerializer,
            RestaurantRepositoryPort restaurantRepositoryPort,
            DiningTableRepositoryPort diningTableRepositoryPort
    ) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.diningTableRepositoryPort = diningTableRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.eventPayloadSerializer = eventPayloadSerializer;
    }

    @Transactional
    public Reservation createReservation(
            Long tenantId,
            Long restaurantId,
            Long tableId,
            Long customerId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ReservationStatus status
    ) {

        if (restaurantId == null || tableId == null) {
            throw new IllegalArgumentException(
                    "Restaurant and table are required."
            );
        }

        // Tenant isolation: the restaurant and table must belong to the caller's tenant.
        // A foreign id is reported as "not found" so other tenants' data is not revealed.
        if (!restaurantRepositoryPort.existsByIdAndTenantId(restaurantId, tenantId)) {
            throw new NotFoundException("Restaurant not found.");
        }

        if (!diningTableRepositoryPort.existsByIdAndTenantIdAndRestaurantId(tableId, tenantId, restaurantId)) {
            throw new NotFoundException("Table not found.");
        }

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Booking start time and end time are required."
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "Booking end time must be after start time."
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Reservation status is required."
            );
        }

        boolean occupied =
                reservationRepositoryPort.existsOverlappingReservation(
                        tenantId,
                        restaurantId,
                        tableId,
                        startTime,
                        endTime,
                        status
                );

        if (occupied) {
            throw new ConflictException(
                    "This table is already reserved for the selected time."
            );
        }

        Reservation reservation = new Reservation(
                tenantId,
                tableId,
                restaurantId,
                customerId,
                startTime,
                endTime,
                status
        );

        Reservation savedReservation =
                reservationRepositoryPort.save(reservation);

        ReservationCreatedEvent eventData =
                new ReservationCreatedEvent(
                        savedReservation.getId(),
                        tenantId,
                        savedReservation.getRestaurantId(),
                        savedReservation.getTableId(),
                        savedReservation.getCustomerId()
                );

        String payload =
                eventPayloadSerializer.serialize(eventData);

        OutboxEvent event = new OutboxEvent(
                "Reservation",
                savedReservation.getId(),
                "RESERVATION_CREATED",
                payload,
                OutboxStatus.PENDING
        );

        outboxRepositoryPort.save(event);

        return savedReservation;
    }
}
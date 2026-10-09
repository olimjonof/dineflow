package com.dineflow.application.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 10, 19, 0);
    private static final LocalDateTime END = START.plusHours(2);

    @Mock
    private ReservationRepositoryPort reservationRepository;
    @Mock
    private OutboxRepositoryPort outboxRepository;
    @Mock
    private EventPayloadSerializer serializer;
    @Mock
    private RestaurantRepositoryPort restaurantRepository;
    @Mock
    private DiningTableRepositoryPort tableRepository;

    private ReservationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationService(
                reservationRepository, outboxRepository, serializer, restaurantRepository, tableRepository);
    }

    /** Restaurant 2 and table 3 belong to tenant 1. */
    private void ownResourcesExist() {
        when(restaurantRepository.existsByIdAndTenantId(2L, 1L)).thenReturn(true);
        when(tableRepository.existsByIdAndTenantIdAndRestaurantId(3L, 1L, 2L)).thenReturn(true);
    }

    @Test
    void rejectsEndTimeNotAfterStartTime() {
        ownResourcesExist();

        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, 2L, 3L, 4L, START, START, ReservationStatus.CONFIRMED));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsMissingTimes() {
        ownResourcesExist();

        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, 2L, 3L, 4L, null, END, ReservationStatus.CONFIRMED));
    }

    @Test
    void rejectsMissingStatus() {
        ownResourcesExist();

        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, 2L, 3L, 4L, START, END, null));
    }

    @Test
    void throwsConflictWhenTableAlreadyReserved() {
        ownResourcesExist();
        when(reservationRepository.existsOverlappingReservation(
                1L, 2L, 3L, START, END, ReservationStatus.CONFIRMED)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                service.createReservation(1L, 2L, 3L, 4L, START, END, ReservationStatus.CONFIRMED));

        verify(reservationRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void savesReservationAndWritesPendingOutboxEvent() {
        ownResourcesExist();
        when(reservationRepository.existsOverlappingReservation(
                1L, 2L, 3L, START, END, ReservationStatus.CONFIRMED)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(serializer.serialize(any())).thenReturn("{}");

        Reservation result = service.createReservation(
                1L, 2L, 3L, 4L, START, END, ReservationStatus.CONFIRMED);

        assertEquals(2L, result.getRestaurantId());
        assertEquals(3L, result.getTableId());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(captor.capture());
        assertEquals(OutboxStatus.PENDING, captor.getValue().getStatus());
        assertEquals("RESERVATION_CREATED", captor.getValue().getType());
    }

    @Test
    void rejectsRestaurantOfAnotherTenant() {
        when(restaurantRepository.existsByIdAndTenantId(2L, 1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                service.createReservation(1L, 2L, 3L, 4L, START, END, ReservationStatus.CONFIRMED));

        verify(reservationRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void rejectsTableOfAnotherTenantOrRestaurant() {
        when(restaurantRepository.existsByIdAndTenantId(2L, 1L)).thenReturn(true);
        when(tableRepository.existsByIdAndTenantIdAndRestaurantId(3L, 1L, 2L)).thenReturn(false);

        assertThrows(NotFoundException.class, () ->
                service.createReservation(1L, 2L, 3L, 4L, START, END, ReservationStatus.CONFIRMED));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsMissingRestaurantOrTable() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, null, 3L, 4L, START, END, ReservationStatus.CONFIRMED));
        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, 2L, null, 4L, START, END, ReservationStatus.CONFIRMED));
    }
}

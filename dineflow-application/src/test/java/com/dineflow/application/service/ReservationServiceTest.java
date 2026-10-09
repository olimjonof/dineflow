package com.dineflow.application.service;

import com.dineflow.application.exception.ConflictException;
import com.dineflow.application.port.EventPayloadSerializer;
import com.dineflow.application.port.OutboxRepositoryPort;
import com.dineflow.application.port.ReservationRepositoryPort;
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

    private ReservationService service;

    @BeforeEach
    void setUp() {
        service = new ReservationService(reservationRepository, outboxRepository, serializer);
    }

    @Test
    void rejectsEndTimeNotAfterStartTime() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, 1L, 1L, 1L, START, START, ReservationStatus.CONFIRMED));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsMissingTimes() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, 1L, 1L, 1L, null, END, ReservationStatus.CONFIRMED));
    }

    @Test
    void rejectsMissingStatus() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createReservation(1L, 1L, 1L, 1L, START, END, null));
    }

    @Test
    void throwsConflictWhenTableAlreadyReserved() {
        when(reservationRepository.existsOverlappingReservation(
                1L, 2L, 3L, START, END, ReservationStatus.CONFIRMED)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                service.createReservation(1L, 2L, 3L, 4L, START, END, ReservationStatus.CONFIRMED));

        verify(reservationRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void savesReservationAndWritesPendingOutboxEvent() {
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
}

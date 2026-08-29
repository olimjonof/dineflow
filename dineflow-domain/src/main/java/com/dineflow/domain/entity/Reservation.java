package com.dineflow.domain.entity;

import com.dineflow.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(
                        name = "idx_reservation_unique_booking",
                        columnList = "restaurant_id, table_id, booking_start_time, status",
                        unique = true
                )
        }
)
public class Reservation extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long restaurantId;
    private Long tableId;
    private Long customerId;

    @NotNull
    private LocalDateTime bookingStartTime;

    @NotNull
    private LocalDateTime bookingEndTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Version
    private Long version;

    public Reservation(
            Long tenantId,
            Long tableId,
            Long restaurantId,
            Long customerId,
            LocalDateTime bookingStartTime,
            LocalDateTime bookingEndTime,
            ReservationStatus status
    ) {
        setTenantId(tenantId);
        this.tableId = tableId;
        this.restaurantId = restaurantId;
        this.customerId = customerId;
        this.bookingStartTime = bookingStartTime;
        this.bookingEndTime = bookingEndTime;
        this.status = status;
    }

    protected Reservation() {
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public Long getTableId() {
        return tableId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getBookingStartTime() {
        return bookingStartTime;
    }

    public LocalDateTime getBookingEndTime() {
        return bookingEndTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }
}
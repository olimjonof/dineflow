package com.dineflow.application.dto;

public class ReservationCreatedEvent {

    private final Long reservationId;
    private final Long tenantId;
    private final Long restaurantId;
    private final Long tableId;
    private final Long customerId;

    public ReservationCreatedEvent(
            Long reservationId,
            Long tenantId,
            Long restaurantId,
            Long tableId,
            Long customerId
    ) {
        this.reservationId = reservationId;
        this.tenantId = tenantId;
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.customerId = customerId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getTenantId() {
        return tenantId;
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
}
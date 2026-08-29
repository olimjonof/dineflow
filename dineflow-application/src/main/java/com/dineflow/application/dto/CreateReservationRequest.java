package com.dineflow.application.dto;

public class CreateReservationRequest {

    private Long tenantId;
    private Long restaurantId;
    private Long tableId;
    private Long customerId;


    public CreateReservationRequest() {
    }

    public CreateReservationRequest(Long tenantId, Long restaurantId, Long tableId, Long customerId) {
        this.tenantId = tenantId;
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.customerId = customerId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
}
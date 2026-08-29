package com.dineflow.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;


@Entity
@Table(name = "dining_tables")

public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    private Long restaurantId;

    @Min(1)
    private int tableNumber;

    @Min(1)
    private int capacity;

    private String locationZone;

    public DiningTable(Long tenantId, Long restaurantId, int tableNumber, int capacity, String locationZone) {
        this.tenantId = tenantId;
        this.restaurantId = restaurantId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.locationZone = locationZone;
    }
    protected DiningTable(){

    }

    public Long getId() {
        return id;
    }

    public String getLocationZone() {
        return locationZone;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }
}

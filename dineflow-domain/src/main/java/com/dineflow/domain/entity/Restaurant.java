package com.dineflow.domain.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.ZoneId;
@Entity
@Table(name = "restaurants")

public class Restaurant extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Name cannot be blank")
    @Column(nullable = false)
    private String name;


    private String address;

    @Column(name = "time_zone", nullable = false)
    private ZoneId timeZone;

    public Restaurant( String name, String address, ZoneId timeZone) {
        this.name = name;
        this.address = address;
        this.timeZone = timeZone;
    }

    protected Restaurant() {
    }


    public Long getId() {
        return id;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

}

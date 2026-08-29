package com.dineflow.application.dto;

import com.dineflow.domain.enums.TenantStatus;

public class CreateTenantRequest {

    private String name;
    private TenantStatus status;

    public CreateTenantRequest(String name, TenantStatus status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public TenantStatus getStatus() {
        return status;
    }
}
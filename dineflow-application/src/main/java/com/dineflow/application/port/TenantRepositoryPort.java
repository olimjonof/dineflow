package com.dineflow.application.port;

import com.dineflow.domain.entity.Tenant;

public interface TenantRepositoryPort {
    Tenant saveTenant(Tenant tenant);
}

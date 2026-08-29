package com.dineflow.application.service;

import com.dineflow.application.port.TenantRepositoryPort;
import com.dineflow.domain.entity.Tenant;
import com.dineflow.domain.enums.TenantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepositoryPort tenantRepositoryPort;

    public TenantService(
            TenantRepositoryPort tenantRepositoryPort
    ) {
        this.tenantRepositoryPort = tenantRepositoryPort;
    }

    @Transactional
    public Tenant createTenant(
            String name,
            TenantStatus status
    ) {

        Tenant tenant = new Tenant(name, status);

        return tenantRepositoryPort.saveTenant(tenant);
    }
}
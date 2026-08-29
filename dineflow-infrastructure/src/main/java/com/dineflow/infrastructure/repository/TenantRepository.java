package com.dineflow.infrastructure.repository;

import com.dineflow.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dineflow.application.port.TenantRepositoryPort;

public interface TenantRepository extends JpaRepository<Tenant,Long>, TenantRepositoryPort{

    @Override
    default Tenant saveTenant(Tenant tenant) {
        return save(tenant);
    }
}

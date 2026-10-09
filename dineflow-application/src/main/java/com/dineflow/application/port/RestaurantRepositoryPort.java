package com.dineflow.application.port;

public interface RestaurantRepositoryPort {

    boolean existsByIdAndTenantId(Long id, Long tenantId);
}

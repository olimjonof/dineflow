package com.dineflow.application.port;

public interface DiningTableRepositoryPort {

    boolean existsByIdAndTenantIdAndRestaurantId(Long id, Long tenantId, Long restaurantId);
}

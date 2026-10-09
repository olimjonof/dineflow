package com.dineflow.infrastructure.repository;

import com.dineflow.application.port.RestaurantRepositoryPort;
import com.dineflow.domain.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryPort {

    @Override
    boolean existsByIdAndTenantId(Long id, Long tenantId);
}

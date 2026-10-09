package com.dineflow.infrastructure.repository;

import com.dineflow.application.port.DiningTableRepositoryPort;
import com.dineflow.domain.entity.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long>, DiningTableRepositoryPort {

    @Override
    boolean existsByIdAndTenantIdAndRestaurantId(Long id, Long tenantId, Long restaurantId);
}

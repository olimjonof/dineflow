package com.dineflow.infrastructure.repository;

import com.dineflow.domain.entity.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
}

package com.dineflow.infrastructure.repository;

import com.dineflow.application.port.OutboxRepositoryPort;
import com.dineflow.domain.entity.OutboxEvent;
import com.dineflow.domain.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository
        extends JpaRepository<OutboxEvent, Long>,
        OutboxRepositoryPort {

    @Override
    List<OutboxEvent> findByStatus(OutboxStatus status);
}
package com.dineflow.application.port;

import com.dineflow.domain.entity.OutboxEvent;
import com.dineflow.domain.enums.OutboxStatus;

import java.util.List;

public interface OutboxRepositoryPort {
    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findByStatus(OutboxStatus status);
}

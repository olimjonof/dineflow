package com.dineflow.infrastructure.messaging;

import com.dineflow.application.service.OutboxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    private final OutboxService outboxService;

    public OutboxScheduler(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        outboxService.publishPendingEvents();
    }
}
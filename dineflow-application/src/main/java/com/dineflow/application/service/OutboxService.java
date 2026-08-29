package com.dineflow.application.service;

import com.dineflow.application.port.MessagePublisher;
import com.dineflow.application.port.OutboxRepositoryPort;
import com.dineflow.domain.entity.OutboxEvent;
import com.dineflow.domain.enums.OutboxStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxService {

    private final OutboxRepositoryPort outboxRepositoryPort;
    private final MessagePublisher messagePublisher;

    public OutboxService(
            OutboxRepositoryPort outboxRepositoryPort,
            MessagePublisher messagePublisher
    ) {
        this.outboxRepositoryPort = outboxRepositoryPort;
        this.messagePublisher = messagePublisher;
    }

    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxRepositoryPort.findByStatus(OutboxStatus.PENDING);

        for (OutboxEvent event : events) {

            messagePublisher.publish(
                    event.getType(),
                    event.getPayload()
            );

            event.markAsProcessed();

            outboxRepositoryPort.save(event);
        }
    }
}
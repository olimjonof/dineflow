package com.dineflow.domain.entity;

import com.dineflow.domain.enums.OutboxStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;

    private Long aggregateId;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    protected OutboxEvent() {
    }

    public OutboxEvent(
            String aggregateType,
            Long aggregateId,
            String type,
            String payload,
            OutboxStatus status
    ) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public Long getAggregateId() {
        return aggregateId;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }
    public void markAsProcessed() {
        this.status = OutboxStatus.PROCESSED;
    }
}
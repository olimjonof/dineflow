package com.dineflow.domain.entity;

import com.dineflow.domain.enums.TenantStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Tenant(String name, TenantStatus status) {
        this.name = name;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
    protected Tenant() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

package com.dineflow.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(
        name = "tenantFilter",
        condition = "tenant_id = :tenantId"
)
public abstract class BaseTenantEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
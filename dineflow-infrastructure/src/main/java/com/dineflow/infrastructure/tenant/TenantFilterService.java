package com.dineflow.infrastructure.tenant;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantFilterService {
    private final SessionFactory sessionFactory;

    public TenantFilterService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public void enableTenantFilter(Long tenantId) {

        Session session = sessionFactory.getCurrentSession();
        Filter filter = session.enableFilter("tenantFilter");

        filter.setParameter("tenantId", tenantId);
    }


    public void enableCurrentTenantFilter() {

        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID is not set");
        }

        enableTenantFilter(tenantId);
    }


}

package com.dineflow.infrastructure.tenant;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor{

    private final TenantFilterService tenantFilterService;

    public TenantInterceptor(TenantFilterService tenantFilterService) {
        this.tenantFilterService = tenantFilterService;
    }


    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ){

        String tenantIdHeader = request.getHeader("X-Tenant-Id");

        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        try {
            Long tenantId = Long.parseLong(tenantIdHeader);

            TenantContext.setTenantId(tenantId);

            return true;

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }
    }


    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        TenantContext.clear();
    }

}
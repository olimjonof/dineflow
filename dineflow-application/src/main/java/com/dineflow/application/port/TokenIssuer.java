package com.dineflow.application.port;

import com.dineflow.application.dto.AccessToken;

public interface TokenIssuer {

    AccessToken issue(Long userId, Long tenantId, String email);
}

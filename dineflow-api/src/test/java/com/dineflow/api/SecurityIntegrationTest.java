package com.dineflow.api;

import com.dineflow.api.controller.AuthController;
import com.dineflow.api.controller.ReservationController;
import com.dineflow.api.exception.GlobalExceptionHandler;
import com.dineflow.application.dto.AccessToken;
import com.dineflow.application.exception.AuthenticationFailedException;
import com.dineflow.application.service.AuthService;
import com.dineflow.application.service.ReservationService;
import com.dineflow.domain.entity.Reservation;
import com.dineflow.infrastructure.config.SecurityConfig;
import com.dineflow.infrastructure.config.WebConfig;
import com.dineflow.infrastructure.security.JwtTokenIssuer;
import com.dineflow.infrastructure.tenant.TenantInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SecurityIntegrationTest.TestApp.class,
        properties = "dineflow.security.jwt.secret=0123456789-0123456789-0123456789-test"
)
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    @Import({
            SecurityConfig.class, WebConfig.class, TenantInterceptor.class, JwtTokenIssuer.class,
            AuthController.class, ReservationController.class, GlobalExceptionHandler.class
    })
    static class TestApp {
    }

    private static final String RESERVATION_BODY = """
            {"tenantId": 999, "restaurantId": 2, "tableId": 3, "customerId": 4,
             "startTime": "2026-10-10T19:00:00", "endTime": "2026-10-10T21:00:00", "status": "CONFIRMED"}
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenIssuer tokenIssuer;

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private AuthService authService;

    private String tokenForTenant(long tenantId) {
        return tokenIssuer.issue(1L, tenantId, "owner@example.com").token();
    }

    @Test
    void reservationsRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(RESERVATION_BODY))
                .andExpect(status().isUnauthorized());

        verify(reservationService, never()).createReservation(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = tokenForTenant(1);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + tampered)
                        .contentType(MediaType.APPLICATION_JSON).content(RESERVATION_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tenantComesFromTokenAndNotFromRequestBodyOrHeader() throws Exception {
        when(reservationService.createReservation(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Reservation(1L, 3L, 2L, 4L,
                        LocalDateTime.of(2026, 10, 10, 19, 0), LocalDateTime.of(2026, 10, 10, 21, 0), null));

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + tokenForTenant(42))
                        .header("X-Tenant-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON).content(RESERVATION_BODY))
                .andExpect(status().isOk());

        verify(reservationService).createReservation(eq(42L), eq(2L), eq(3L), eq(4L), any(), any(), any());
    }

    @Test
    void loginIsPublicAndReturnsBearerToken() throws Exception {
        when(authService.login("owner@example.com", "secret-pass")).thenReturn(new AccessToken("jwt-value", 3600));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@example.com\",\"password\":\"secret-pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void wrongCredentialsReturn401() throws Exception {
        when(authService.login(any(), any())).thenThrow(new AuthenticationFailedException("Invalid email or password."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"owner@example.com\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerRejectsShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantName\":\"R\",\"name\":\"Owner\",\"email\":\"o@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(), any(), any(), any());
    }

    @Test
    void registerReturns201WithToken() throws Exception {
        when(authService.register("R", "Owner", "o@example.com", "long-enough-pass"))
                .thenReturn(new AccessToken("new-jwt", 3600));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantName\":\"R\",\"name\":\"Owner\",\"email\":\"o@example.com\",\"password\":\"long-enough-pass\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("new-jwt"));
    }
}

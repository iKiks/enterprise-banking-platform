package com.bank.app.auth;

import com.bank.app.auth.dto.LoginRequest;
import com.bank.app.auth.dto.RegisterRequest;
import com.bank.app.auth.dto.TokenResponse;
import com.bank.app.auth.service.AuthService;
import com.bank.app.auth.repository.UserRepository;
import com.bank.app.audit.service.AuditLogService;
import com.bank.app.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@SuppressWarnings({"deprecation", "resource"})
public class AuthIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("banking")
            .withUsername("bank_user")
            .withPassword("bank_pass");

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("DB_URL", postgres::getJdbcUrl);
        r.add("DB_USERNAME", postgres::getUsername);
        r.add("DB_PASSWORD", postgres::getPassword);
        r.add("REDIS_HOST", () -> redis.getHost());
        r.add("REDIS_PORT", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    void register_login_refresh_flow() {
        RegisterRequest reg = new RegisterRequest("inttest@example.com", "Password123!", "Integration Test");
        authService.register(reg);

        assertThat(userRepository.existsByEmail(reg.email())).isTrue();

        TokenResponse tokens = authService.login(new LoginRequest(reg.email(), reg.password()));
        assertThat(tokens).isNotNull();
        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();

        com.bank.app.auth.dto.RefreshTokenRequest refreshReq = new com.bank.app.auth.dto.RefreshTokenRequest(tokens.refreshToken());
        TokenResponse refreshed = authService.refresh(refreshReq);
        assertThat(refreshed).isNotNull();
    }
}

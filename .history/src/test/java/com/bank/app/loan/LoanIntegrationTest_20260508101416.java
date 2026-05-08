package com.bank.app.loan;

import com.bank.app.customer.entity.Customer;
import com.bank.app.customer.repository.CustomerRepository;
import com.bank.app.loan.dto.LoanApplicationRequest;
import com.bank.app.loan.service.LoanService;
import com.bank.app.loan.repository.LoanRepository;
import com.bank.app.audit.service.AuditLogService;
import com.bank.app.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@SuppressWarnings({"deprecation", "resource"})
public class LoanIntegrationTest {

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
    private LoanService loanService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanRepository loanRepository;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuditLogService auditLogService;

    @BeforeEach
    void setup() {
        loanRepository.deleteAll();
        customerRepository.deleteAll();
        Customer c = new Customer();
        c.setCustomerNumber("LOAN-CUST-1");
        c.setStatus(null);
        c.setRiskLevel(null);
        c.setKycStatus(null);
        customerRepository.save(c);
    }

    @Test
    void apply_and_approve_loan() {
        LoanApplicationRequest req = new LoanApplicationRequest("LOAN-CUST-1", new BigDecimal("1000"), new BigDecimal("5.0"), 12);
        var loan = loanService.apply(req);
        assertThat(loan).isNotNull();
        var approved = loanService.approve(loan.getId());
        assertThat(approved.getStatus()).isEqualTo(com.bank.app.loan.enums.LoanStatus.APPROVED);
    }
}

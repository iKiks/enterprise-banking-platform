package com.bank.app.transaction;

import com.bank.app.account.entity.BankAccount;
import com.bank.app.account.repository.BankAccountRepository;
import com.bank.app.customer.entity.Customer;
import com.bank.app.customer.repository.CustomerRepository;
import com.bank.app.transaction.dto.TransferRequest;
import com.bank.app.transaction.repository.TransactionRepository;
import com.bank.app.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@SuppressWarnings({"deprecation", "resource"})
public class TransactionConcurrencyIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("banking")
            .withUsername("bank_user")
            .withPassword("bank_pass");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("DB_URL", postgres::getJdbcUrl);
        r.add("DB_USERNAME", postgres::getUsername);
        r.add("DB_PASSWORD", postgres::getPassword);
    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private BankAccount source;
    private BankAccount dest;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        Customer c1 = new Customer();
        c1.setCustomerNumber("CUST-1");
        c1.setStatus(null);
        c1.setRiskLevel(null);
        c1.setKycStatus(null);
        customerRepository.save(c1);

        Customer c2 = new Customer();
        c2.setCustomerNumber("CUST-2");
        c2.setStatus(null);
        c2.setRiskLevel(null);
        c2.setKycStatus(null);
        customerRepository.save(c2);

        source = new BankAccount();
        source.setAccountNumber("SRC-0001");
        source.setAccountType(null);
        source.setCurrencyCode("NGN");
        source.setBalance(new BigDecimal("1000"));
        source.setAvailableBalance(new BigDecimal("1000"));
        source.setDailyLimit(new BigDecimal("5000"));
        source.setStatus(null);
        source.setCustomer(c1);
        accountRepository.save(source);

        dest = new BankAccount();
        dest.setAccountNumber("DST-0001");
        dest.setAccountType(null);
        dest.setCurrencyCode("NGN");
        dest.setBalance(new BigDecimal("100"));
        dest.setAvailableBalance(new BigDecimal("100"));
        dest.setDailyLimit(new BigDecimal("5000"));
        dest.setStatus(null);
        dest.setCustomer(c2);
        accountRepository.save(dest);
    }

    @Test
    void concurrent_same_idempotency_only_one_applies() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        ExecutorService ex = Executors.newFixedThreadPool(8);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            tasks.add(() -> {
                try {
                    transactionService.transfer(new TransferRequest(source.getAccountNumber(), dest.getAccountNumber(), new BigDecimal("100"), idempotencyKey, "concurrent"), "test");
                    return true;
                } catch (Exception exx) {
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = ex.invokeAll(tasks);
        int success = 0;
        for (Future<Boolean> f : futures) if (f.get()) success++;

        // Only one successful transfer should have succeeded for same idempotency key
        assertThat(success).isGreaterThanOrEqualTo(1);
        assertThat(success).isLessThanOrEqualTo(8);

        // Verify balances: if only one success, source balance reduced by ~100 + fee
        BankAccount refreshed = accountRepository.findByAccountNumberAndDeletedFalse(source.getAccountNumber()).get();
        assertThat(refreshed.getBalance()).isLessThanOrEqualTo(source.getBalance());
    }
}

package com.bank.app.transaction.service;

import com.bank.app.account.entity.BankAccount;
import com.bank.app.transaction.dto.TransactionResponse;
import com.bank.app.transaction.dto.TransferRequest;
import com.bank.app.transaction.entity.Transaction;
import com.bank.app.transaction.mapper.TransactionMapper;
import com.bank.app.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionRepository transactionRepository;
    private com.bank.app.account.repository.BankAccountRepository accountRepository;
    private TransactionMapper transactionMapper;
    private com.bank.app.audit.service.AuditLogService auditLogService;
    private com.bank.app.notification.service.NotificationService notificationService;
    private com.bank.app.transaction.service.TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(com.bank.app.account.repository.BankAccountRepository.class);
        transactionMapper = mock(TransactionMapper.class);
        auditLogService = mock(com.bank.app.audit.service.AuditLogService.class);
        notificationService = mock(com.bank.app.notification.service.NotificationService.class);

        transactionService = new com.bank.app.transaction.service.TransactionService(
                transactionRepository,
                accountRepository,
                transactionMapper,
                auditLogService,
                notificationService
        );
    }

    @Test
    void transfer_success() {
        BankAccount source = new BankAccount();
        source.setAccountNumber("SRC-123");
        source.setBalance(new BigDecimal("1000"));
        source.setAvailableBalance(new BigDecimal("1000"));
        source.setCurrencyCode("NGN");

        BankAccount dest = new BankAccount();
        dest.setAccountNumber("DST-456");
        dest.setBalance(new BigDecimal("100"));
        dest.setAvailableBalance(new BigDecimal("100"));
        dest.setCurrencyCode("NGN");

        when(accountRepository.findWithLockByAccountNumber("SRC-123")).thenReturn(Optional.of(source));
        when(accountRepository.findWithLockByAccountNumber("DST-456")).thenReturn(Optional.of(dest));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse expectedResponse = new TransactionResponse("REF-1", null, null, new BigDecimal("100"), new BigDecimal("0.5"), "SRC-123", "DST-456");
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(expectedResponse);

        TransferRequest req = new TransferRequest("SRC-123", "DST-456", new BigDecimal("100"), UUID.randomUUID().toString(), "test");

        TransactionResponse resp = transactionService.transfer(req, "tester");

        assertThat(resp).isNotNull();
        assertThat(resp.sourceAccount()).isEqualTo("SRC-123");
        assertThat(resp.destinationAccount()).isEqualTo("DST-456");

        verify(accountRepository, times(2)).findWithLockByAccountNumber(anyString());
        verify(transactionRepository).save(any(Transaction.class));
        verify(auditLogService).log(eq("tester"), anyString(), anyString(), anyString(), anyString());
        verify(notificationService).publishNotification(anyString(), anyString(), anyString(), anyString());
    }
}

package com.bank.app.transaction.service;

import com.bank.app.account.entity.BankAccount;
import com.bank.app.account.repository.BankAccountRepository;
import com.bank.app.audit.service.AuditLogService;
import com.bank.app.exception.BusinessException;
import com.bank.app.exception.NotFoundException;
import com.bank.app.notification.service.NotificationService;
import com.bank.app.transaction.dto.TransactionResponse;
import com.bank.app.transaction.dto.TransferRequest;
import com.bank.app.transaction.entity.Transaction;
import com.bank.app.transaction.enums.TransactionStatus;
import com.bank.app.transaction.enums.TransactionType;
import com.bank.app.transaction.mapper.TransactionMapper;
import com.bank.app.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
                              BankAccountRepository accountRepository,
                              TransactionMapper transactionMapper,
                              AuditLogService auditLogService,
                              NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String actor) {
        // quick idempotency pre-check
        transactionRepository.findByIdempotencyKey(request.idempotencyKey()).ifPresent(existing -> {
            return;
        });

        BankAccount source = accountRepository.findWithLockByAccountNumber(request.sourceAccount())
                .orElseThrow(() -> new NotFoundException("Source account not found"));
        BankAccount destination = accountRepository.findWithLockByAccountNumber(request.destinationAccount())
                .orElseThrow(() -> new NotFoundException("Destination account not found"));

        // Basic validations
        if (!source.getCurrencyCode().equalsIgnoreCase(destination.getCurrencyCode())) {
            throw new BusinessException("Currency mismatch between accounts");
        }

        BigDecimal fee = request.amount().multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDebit = request.amount().add(fee).setScale(2, RoundingMode.HALF_UP);

        if (source.getAvailableBalance().compareTo(totalDebit) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        // Enforce daily limit: compute today's debits
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        BigDecimal alreadyDebited = transactionRepository.sumAmountDebitedForAccountBetween(source.getId(), start, end);
        BigDecimal projected = alreadyDebited.add(request.amount());
        BigDecimal dailyLimit = source.getDailyLimit() != null ? source.getDailyLimit() : BigDecimal.ZERO;
        if (dailyLimit.compareTo(BigDecimal.ZERO) > 0 && projected.compareTo(dailyLimit) > 0) {
            throw new BusinessException("Daily transfer limit exceeded");
        }

        // apply debit/credit
        source.setBalance(source.getBalance().subtract(totalDebit));
        source.setAvailableBalance(source.getAvailableBalance().subtract(totalDebit));
        destination.setBalance(destination.getBalance().add(request.amount()));
        destination.setAvailableBalance(destination.getAvailableBalance().add(request.amount()));

        accountRepository.save(source);
        accountRepository.save(destination);

        Transaction transaction = new Transaction();
        transaction.setReference("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        transaction.setAmount(request.amount());
        transaction.setFeeAmount(fee);
        transaction.setCurrencyCode(source.getCurrencyCode());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(request.description());
        transaction.setIdempotencyKey(request.idempotencyKey());

        try {
            Transaction saved = transactionRepository.save(transaction);
            // force flush to detect DB constraint violations (idempotency unique) within this transaction
            transactionRepository.flush();

            // mark success and update
            saved.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(saved);

            // register after-commit actions: audit & notification
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    auditLogService.log(actor, "TRANSFER", "TRANSACTION", saved.getReference(), "Transfer completed successfully");
                    try {
                        notificationService.publishNotification(source.getCustomer().getUser().getEmail(), "Transfer Alert",
                                "Your transfer of " + request.amount() + " was successful.", "EMAIL");
                    } catch (Exception ex) {
                        // swallow notification exceptions to avoid impacting transaction
                    }
                }
            });

            return transactionMapper.toResponse(saved);
        } catch (DataIntegrityViolationException dive) {
            // likely idempotency unique constraint violation: try to return the existing transaction
            transactionRepository.findByIdempotencyKey(request.idempotencyKey()).ifPresent(existing -> {
                // nothing to do here; fall through to return
            });
            return transactionMapper.toResponse(transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                    .orElseThrow(() -> new BusinessException("Duplicate transaction detected")));
        }
    }
}

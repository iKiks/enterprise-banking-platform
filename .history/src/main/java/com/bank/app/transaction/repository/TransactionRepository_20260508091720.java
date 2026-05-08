package com.bank.app.transaction.repository;

import com.bank.app.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Page<Transaction> findByDeletedFalse(Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sourceAccount.id = :accountId AND t.deleted = false AND t.createdAt >= :start AND t.createdAt < :end")
    BigDecimal sumAmountDebitedForAccountBetween(@Param("accountId") Long accountId,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
}

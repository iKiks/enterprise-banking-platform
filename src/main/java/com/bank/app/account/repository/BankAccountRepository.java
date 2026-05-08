package com.bank.app.account.repository;

import com.bank.app.account.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumberAndDeletedFalse(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankAccount> findWithLockByAccountNumber(String accountNumber);
}

package com.bank.app.account.repository;

import com.bank.app.account.entity.AccountLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountLimitRepository extends JpaRepository<AccountLimit, Long> {
    Optional<AccountLimit> findByAccountId(Long accountId);
}

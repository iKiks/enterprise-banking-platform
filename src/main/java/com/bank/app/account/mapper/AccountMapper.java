package com.bank.app.account.mapper;

import com.bank.app.account.dto.AccountResponse;
import com.bank.app.account.entity.BankAccount;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponse toResponse(BankAccount account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getStatus(),
                account.getCurrencyCode(),
                account.getBalance(),
                account.getAvailableBalance()
        );
    }
}

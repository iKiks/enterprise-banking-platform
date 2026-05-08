package com.bank.app.account.dto;

import com.bank.app.account.enums.AccountStatus;
import com.bank.app.account.enums.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        String accountNumber,
        AccountType accountType,
        AccountStatus status,
        String currencyCode,
        BigDecimal balance,
        BigDecimal availableBalance
) {
}

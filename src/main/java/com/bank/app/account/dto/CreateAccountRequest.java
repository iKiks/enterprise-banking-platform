package com.bank.app.account.dto;

import com.bank.app.account.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "Customer number is required")
        String customerNumber,
        @NotNull(message = "Account type is required")
        AccountType accountType,
        @NotBlank(message = "Currency code is required")
        String currencyCode,
        @NotNull(message = "Daily limit is required")
        BigDecimal dailyLimit
) {
}

package com.bank.app.loan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanApplicationRequest(
        @NotBlank(message = "Customer number is required")
        String customerNumber,
        @NotNull @Positive
        BigDecimal principalAmount,
        @NotNull @Positive
        BigDecimal interestRate,
        @NotNull
        Integer tenorMonths
) {
}

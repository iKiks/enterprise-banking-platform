package com.bank.app.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Source account is required")
        String sourceAccount,
        @NotBlank(message = "Destination account is required")
        String destinationAccount,
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey,
        String description
) {
}

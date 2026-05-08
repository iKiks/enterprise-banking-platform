package com.bank.app.transaction.dto;

import com.bank.app.transaction.enums.TransactionStatus;
import com.bank.app.transaction.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionResponse(
        String reference,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal feeAmount,
        String sourceAccount,
        String destinationAccount
) {
}

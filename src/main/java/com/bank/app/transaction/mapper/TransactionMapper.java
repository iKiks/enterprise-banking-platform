package com.bank.app.transaction.mapper;

import com.bank.app.transaction.dto.TransactionResponse;
import com.bank.app.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getReference(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getFeeAmount(),
                transaction.getSourceAccount() != null ? transaction.getSourceAccount().getAccountNumber() : null,
                transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getAccountNumber() : null
        );
    }
}

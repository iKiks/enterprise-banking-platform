package com.bank.app.card.dto;

public record CardResponse(
        String cardNumber,
        String cardHolderName,
        boolean active,
        boolean blocked
) {
}

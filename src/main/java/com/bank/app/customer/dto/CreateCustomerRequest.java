package com.bank.app.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank(message = "User email is required")
        String userEmail,
        String bvn,
        String nin
) {
}

package com.bank.app.customer.dto;

import com.bank.app.customer.enums.CustomerStatus;
import com.bank.app.customer.enums.KycStatus;
import com.bank.app.customer.enums.RiskLevel;

public record CustomerResponse(
        String customerNumber,
        String email,
        CustomerStatus status,
        RiskLevel riskLevel,
        KycStatus kycStatus
) {
}

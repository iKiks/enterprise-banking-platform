package com.bank.app.customer.mapper;

import com.bank.app.customer.dto.CustomerResponse;
import com.bank.app.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerNumber(),
                customer.getUser() != null ? customer.getUser().getEmail() : null,
                customer.getStatus(),
                customer.getRiskLevel(),
                customer.getKycStatus()
        );
    }
}

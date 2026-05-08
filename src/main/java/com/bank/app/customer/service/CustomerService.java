package com.bank.app.customer.service;

import com.bank.app.auth.entity.User;
import com.bank.app.auth.repository.UserRepository;
import com.bank.app.customer.dto.CreateCustomerRequest;
import com.bank.app.customer.dto.CustomerResponse;
import com.bank.app.customer.entity.Customer;
import com.bank.app.customer.enums.CustomerStatus;
import com.bank.app.customer.enums.KycStatus;
import com.bank.app.customer.enums.RiskLevel;
import com.bank.app.customer.mapper.CustomerMapper;
import com.bank.app.customer.repository.CustomerRepository;
import com.bank.app.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.userEmail())
                .orElseThrow(() -> new NotFoundException("User not found for customer creation"));

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setCustomerNumber("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setKycStatus(KycStatus.PENDING);
        customer.setRiskLevel(RiskLevel.MEDIUM);
        customer.setBvn(request.bvn());
        customer.setNin(request.nin());

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepository.findByDeletedFalse(pageable).map(customerMapper::toResponse);
    }

    @Transactional
    public CustomerResponse freeze(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumberAndDeletedFalse(customerNumber)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        customer.setStatus(CustomerStatus.FROZEN);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse unfreeze(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumberAndDeletedFalse(customerNumber)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        customer.setStatus(CustomerStatus.ACTIVE);
        return customerMapper.toResponse(customerRepository.save(customer));
    }
}

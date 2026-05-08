package com.bank.app.customer.repository;

import com.bank.app.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerNumberAndDeletedFalse(String customerNumber);
    Page<Customer> findByDeletedFalse(Pageable pageable);
}

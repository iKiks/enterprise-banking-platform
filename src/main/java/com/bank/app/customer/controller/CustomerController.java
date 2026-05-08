package com.bank.app.customer.controller;

import com.bank.app.common.api.ApiResponse;
import com.bank.app.customer.dto.CreateCustomerRequest;
import com.bank.app.customer.dto.CustomerResponse;
import com.bank.app.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','ADMIN')")
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.success("Customer created", customerService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','ADMIN','AUDITOR')")
    public ApiResponse<Page<CustomerResponse>> list(Pageable pageable) {
        return ApiResponse.success("Customers fetched", customerService.list(pageable));
    }

    @PostMapping("/{customerNumber}/freeze")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ApiResponse<CustomerResponse> freeze(@PathVariable String customerNumber) {
        return ApiResponse.success("Customer frozen", customerService.freeze(customerNumber));
    }

    @PostMapping("/{customerNumber}/unfreeze")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ApiResponse<CustomerResponse> unfreeze(@PathVariable String customerNumber) {
        return ApiResponse.success("Customer unfrozen", customerService.unfreeze(customerNumber));
    }
}

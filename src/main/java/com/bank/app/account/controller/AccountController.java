package com.bank.app.account.controller;

import com.bank.app.account.dto.AccountResponse;
import com.bank.app.account.dto.CreateAccountRequest;
import com.bank.app.account.service.AccountService;
import com.bank.app.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BANKER','MANAGER','ADMIN')")
    public ApiResponse<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        return ApiResponse.success("Account created successfully", accountService.createAccount(request));
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','BANKER','MANAGER','ADMIN','AUDITOR')")
    public ApiResponse<AccountResponse> get(@PathVariable String accountNumber) {
        return ApiResponse.success("Account fetched", accountService.getByAccountNumber(accountNumber));
    }

    @PostMapping("/{accountNumber}/freeze")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ApiResponse<AccountResponse> freeze(@PathVariable String accountNumber) {
        return ApiResponse.success("Account frozen", accountService.freezeAccount(accountNumber));
    }

    @PostMapping("/{accountNumber}/close")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ApiResponse<AccountResponse> close(@PathVariable String accountNumber) {
        return ApiResponse.success("Account closed", accountService.closeAccount(accountNumber));
    }
}

package com.bank.app.loan.controller;

import com.bank.app.common.api.ApiResponse;
import com.bank.app.loan.dto.LoanApplicationRequest;
import com.bank.app.loan.entity.Loan;
import com.bank.app.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('CUSTOMER','BANKER','MANAGER')")
    public ApiResponse<Loan> apply(@Valid @RequestBody LoanApplicationRequest request) {
        return ApiResponse.success("Loan application submitted", loanService.apply(request));
    }

    @PostMapping("/{loanId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ApiResponse<Loan> approve(@PathVariable Long loanId) {
        return ApiResponse.success("Loan approved", loanService.approve(loanId));
    }
}

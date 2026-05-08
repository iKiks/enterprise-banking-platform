package com.bank.app.transaction.controller;

import com.bank.app.common.api.ApiResponse;
import com.bank.app.transaction.dto.TransactionResponse;
import com.bank.app.transaction.dto.TransferRequest;
import com.bank.app.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','BANKER','MANAGER','ADMIN')")
    public ApiResponse<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request, Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "system";
        return ApiResponse.success("Transfer successful", transactionService.transfer(request, actor));
    }
}

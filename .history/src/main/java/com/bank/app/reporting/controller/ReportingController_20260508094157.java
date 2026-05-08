package com.bank.app.reporting.controller;

import com.bank.app.common.api.ApiResponse;
import com.bank.app.reporting.service.ReportingService;
import com.bank.app.transaction.entity.Transaction;
import com.bank.app.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reporting")
public class ReportingController {

    private final ReportingService reportingService;
    private final TransactionRepository transactionRepository;

    public ReportingController(ReportingService reportingService, TransactionRepository transactionRepository) {
        this.reportingService = reportingService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR')")
    public ApiResponse<?> daily(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate d = date == null ? LocalDate.now() : date;
        return ApiResponse.success("Daily summary", reportingService.dailySummary(d));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR')")
    public ApiResponse<Page<Transaction>> transactions(Pageable pageable) {
        return ApiResponse.success("Transaction report generated", transactionRepository.findByDeletedFalse(pageable));
    }
}

package com.bank.app.audit.controller;

import com.bank.app.audit.entity.AuditLog;
import com.bank.app.audit.repository.AuditLogRepository;
import com.bank.app.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    public ApiResponse<List<AuditLog>> logs() {
        return ApiResponse.success("Audit logs fetched", auditLogRepository.findAll());
    }
}

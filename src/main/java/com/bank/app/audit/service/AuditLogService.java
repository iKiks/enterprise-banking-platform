package com.bank.app.audit.service;

import com.bank.app.audit.entity.AuditLog;
import com.bank.app.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String actor, String action, String resource, String resourceId, String details) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setResource(resource);
        log.setResourceId(resourceId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}

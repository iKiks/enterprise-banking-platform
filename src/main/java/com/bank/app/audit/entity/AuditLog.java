package com.bank.app.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String actor;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String resource;

    private String resourceId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

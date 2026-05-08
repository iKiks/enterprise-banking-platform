package com.bank.app.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_history")
@Getter
@Setter
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean success;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime loginAt = LocalDateTime.now();
}

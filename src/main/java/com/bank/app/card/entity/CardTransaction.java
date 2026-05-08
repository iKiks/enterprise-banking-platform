package com.bank.app.card.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_transactions")
@Getter
@Setter
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String merchantName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

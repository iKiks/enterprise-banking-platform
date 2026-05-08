package com.bank.app.transaction.entity;

import com.bank.app.account.entity.BankAccount;
import com.bank.app.shared.entity.BaseEntity;
import com.bank.app.transaction.enums.TransactionStatus;
import com.bank.app.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private BankAccount sourceAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private BankAccount destinationAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal feeAmount;

    @Column(nullable = false)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String description;

    @Column(unique = true)
    private String idempotencyKey;
}

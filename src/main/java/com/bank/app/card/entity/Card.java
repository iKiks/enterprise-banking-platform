package com.bank.app.card.entity;

import com.bank.app.account.entity.BankAccount;
import com.bank.app.customer.entity.Customer;
import com.bank.app.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private BankAccount account;

    @Column(unique = true)
    private String cardNumber;

    @Column(unique = true)
    private String panMasked;

    private String cardHolderName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean blocked = false;

    @Column(nullable = false)
    private boolean frozen = false;
}

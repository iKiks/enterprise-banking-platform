package com.bank.app.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kyc_documents")
@Getter
@Setter
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String documentUrl;

    @Column(nullable = false)
    private boolean verified;
}

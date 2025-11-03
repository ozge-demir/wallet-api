package com.ozgedemir.wallet.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String name;
    @Column(nullable=false)
    private String surname;
    @Column(nullable=false, unique=true)
    private String tckn;
    @Column(nullable=false, unique=true)
    private String username;
    @Column(name="password_hash", nullable=false)
    private String passwordHash;
    @Column(nullable=false)
    private String role; // CUSTOMER or EMPLOYEE
}


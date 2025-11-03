package com.ozgedemir.wallet.domain.entities;

import com.ozgedemir.wallet.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name="wallet_name", nullable=false)
    private String walletName;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=3)
    private Currency currency;

    private boolean activeForShopping;
    private boolean activeForWithdraw;

    @Column(nullable=false, precision=19, scale=2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable=false, precision=19, scale=2)
    private BigDecimal usableBalance = BigDecimal.ZERO;

    @Version
    private Integer version;
}


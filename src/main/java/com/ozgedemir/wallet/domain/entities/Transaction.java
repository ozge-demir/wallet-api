package com.ozgedemir.wallet.domain.entities;

import com.ozgedemir.wallet.domain.enums.OppositePartyType;
import com.ozgedemir.wallet.domain.enums.TransactionStatus;
import com.ozgedemir.wallet.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable=false)
    private Wallet wallet;

    @Column(nullable=false, precision=19, scale=2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name="opposite_party_type", nullable=false)
    private OppositePartyType oppositePartyType;

    @Column(name="opposite_party", nullable=false, length=64)
    private String oppositeParty;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TransactionStatus status;

    @Column(name="created_at", nullable=false)
    private Instant createdAt = Instant.now();
}


package com.ozgedemir.wallet.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "idempotent_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idempotency_key","endpoint"}))
@Getter
@Setter
@NoArgsConstructor
public class IdempotentRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 64, updatable = false)
    private String idempotencyKey;

    @Column(nullable = false, length = 120, updatable = false)
    private String endpoint;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
}

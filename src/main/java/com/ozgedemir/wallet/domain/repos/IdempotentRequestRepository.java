package com.ozgedemir.wallet.domain.repos;

import com.ozgedemir.wallet.domain.entities.IdempotentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IdempotentRequestRepository extends JpaRepository<IdempotentRequest, Long> {
    Optional<IdempotentRequest> findByIdempotencyKeyAndEndpoint(String key, String endpoint);
}

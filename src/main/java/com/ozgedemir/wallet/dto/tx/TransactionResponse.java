package com.ozgedemir.wallet.dto.tx;

import com.ozgedemir.wallet.domain.enums.OppositePartyType;
import com.ozgedemir.wallet.domain.enums.TransactionStatus;
import com.ozgedemir.wallet.domain.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long walletId,
        BigDecimal amount,
        TransactionType type,
        OppositePartyType oppositePartyType,
        String oppositeParty,
        TransactionStatus status,
        Instant createdAt
) {}

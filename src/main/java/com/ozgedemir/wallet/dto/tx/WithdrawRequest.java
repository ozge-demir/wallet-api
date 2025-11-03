package com.ozgedemir.wallet.dto.tx;

import com.ozgedemir.wallet.domain.enums.OppositePartyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WithdrawRequest(
        @NotNull Long walletId,
        @Positive BigDecimal amount,
        @NotNull OppositePartyType oppositePartyType,
        String destination
) {}
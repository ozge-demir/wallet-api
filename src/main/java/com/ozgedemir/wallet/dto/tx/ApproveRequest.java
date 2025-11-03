package com.ozgedemir.wallet.dto.tx;

import com.ozgedemir.wallet.domain.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;

public record ApproveRequest(
        @NotNull TransactionStatus status
) {}
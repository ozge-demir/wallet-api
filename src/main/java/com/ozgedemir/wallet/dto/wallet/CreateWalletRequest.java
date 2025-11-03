package com.ozgedemir.wallet.dto.wallet;

import com.ozgedemir.wallet.domain.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
        @NotNull
        Long customerId,
        @NotBlank
        String walletName,
        @NotNull
        Currency currency,
        boolean activeForShopping,
        boolean activeForWithdraw
) {}

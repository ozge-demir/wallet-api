package com.ozgedemir.wallet.dto.wallet;

import com.ozgedemir.wallet.domain.enums.Currency;

import java.math.BigDecimal;

public record WalletResponse(
        Long id,
        Long customerId,
        String walletName,
        Currency currency,
        boolean activeForShopping,
        boolean activeForWithdraw,
        BigDecimal balance,
        BigDecimal usableBalance
) {}

package com.ozgedemir.wallet.domain.repos;

import com.ozgedemir.wallet.domain.entities.Wallet;
import com.ozgedemir.wallet.domain.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    List<Wallet> findByCustomerId(Long customerId);
    List<Wallet> findByCurrency(Currency currency);
    List<Wallet> findByCustomerIdAndCurrency(Long customerId, Currency currency);

}

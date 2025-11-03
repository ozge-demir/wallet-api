package com.ozgedemir.wallet.service;

import com.ozgedemir.wallet.domain.entities.Customer;
import com.ozgedemir.wallet.domain.entities.Wallet;
import com.ozgedemir.wallet.domain.enums.Currency;
import com.ozgedemir.wallet.dto.wallet.CreateWalletRequest;
import com.ozgedemir.wallet.dto.wallet.WalletResponse;
import com.ozgedemir.wallet.domain.repos.CustomerRepository;
import com.ozgedemir.wallet.domain.repos.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletService {

    private final WalletRepository wallets;
    private final CustomerRepository customers;

    public WalletService(WalletRepository wallets, CustomerRepository customers) {
        this.wallets = wallets;
        this.customers = customers;
    }

    @Transactional
    public WalletResponse create(CreateWalletRequest req) {
        Customer c = customers.findById(req.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + req.customerId()));

        Wallet w = new Wallet();
        w.setCustomer(c);
        w.setWalletName(req.walletName());
        w.setCurrency(req.currency());
        w.setActiveForShopping(req.activeForShopping());
        w.setActiveForWithdraw(req.activeForWithdraw());

        Wallet saved = wallets.save(w);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public WalletResponse get(Long id) {
        Wallet w = wallets.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found: " + id));
        return map(w);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> list(Long customerId, Currency currency) {
        List<Wallet> result;

        if (customerId != null && currency != null) {
            result = wallets.findByCustomerIdAndCurrency(customerId, currency);
        } else if (customerId != null) {
            result = wallets.findByCustomerId(customerId);
        } else if (currency != null) {
            result = wallets.findByCurrency(currency);
        } else {
            result = wallets.findAll();
        }

        return result.stream().map(this::map).toList();
    }


    private WalletResponse map(Wallet w) {
        return new WalletResponse(
                w.getId(),
                w.getCustomer().getId(),
                w.getWalletName(),
                w.getCurrency(),
                w.isActiveForShopping(),
                w.isActiveForWithdraw(),
                w.getBalance(),
                w.getUsableBalance()
        );
    }
}

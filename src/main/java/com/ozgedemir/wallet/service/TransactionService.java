package com.ozgedemir.wallet.service;

import com.ozgedemir.wallet.domain.entities.Transaction;
import com.ozgedemir.wallet.domain.entities.Wallet;
import com.ozgedemir.wallet.domain.enums.TransactionStatus;
import com.ozgedemir.wallet.domain.enums.TransactionType;
import com.ozgedemir.wallet.dto.tx.*;
import com.ozgedemir.wallet.domain.repos.TransactionRepository;
import com.ozgedemir.wallet.domain.repos.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private static final BigDecimal THRESHOLD = new BigDecimal("1000");

    private final WalletRepository wallets;
    private final TransactionRepository txs;

    public TransactionService(WalletRepository wallets, TransactionRepository txs) {
        this.wallets = wallets; this.txs = txs;
    }

    // ---------- DEPOSIT ----------
    @Transactional
    public TransactionResponse deposit(DepositRequest req) {
        Wallet w = wallets.findById(req.walletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        boolean pending = req.amount().compareTo(THRESHOLD) > 0;

        Transaction tx = new Transaction();
        tx.setWallet(w);
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(req.amount());
        tx.setOppositePartyType(req.oppositePartyType());
        tx.setOppositeParty(req.source());
        tx.setStatus(pending ? TransactionStatus.PENDING : TransactionStatus.APPROVED);

        // bakiye yansımaları
        if (pending) {
            // sadece balance artar
            w.setBalance(w.getBalance().add(req.amount()));
        } else {
            // balance + usableBalance artar
            w.setBalance(w.getBalance().add(req.amount()));
            w.setUsableBalance(w.getUsableBalance().add(req.amount()));
        }

        // persist
        Transaction saved = txs.save(tx);
        // Wallet JPA dirty checking ile güncellenecek

        return map(saved);
    }

    // ---------- LIST ----------
    @Transactional(readOnly = true)
    public Page<TransactionResponse> list(Long walletId,
                                          Pageable p) {
        return txs.findByWalletId(walletId, p).map(this::map);
    }

    // ---------- APPROVE/DENY ----------
    @Transactional
    public TransactionResponse approve(Long txId, ApproveRequest req) {
        Transaction tx = txs.findById(txId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        // Yalnızca APPROVED/DENIED kabul et
        if (req.status() == TransactionStatus.PENDING) {
            throw new IllegalArgumentException("status must be APPROVED or DENIED");
        }

        // Zaten finalize ise 409
        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is already finalized");
        }

        Wallet w = tx.getWallet();

        switch (tx.getType()) {
            case DEPOSIT -> {
                if (req.status() == TransactionStatus.APPROVED) {
                    // pending deposit -> usableBalance + amount (balance zaten + yapılmıştı)
                    w.setUsableBalance(w.getUsableBalance().add(tx.getAmount()));
                } else { // DENIED
                    // pending deposit reddi -> balance - amount (eklenen geri alınır)
                    w.setBalance(w.getBalance().subtract(tx.getAmount()));
                }
            }
            case WITHDRAW -> {
                if (req.status() == TransactionStatus.APPROVED) {
                    // pending withdraw onayı -> balance - amount (usable zaten - yapılmıştı)
                    w.setBalance(w.getBalance().subtract(tx.getAmount()));
                } else { // DENIED
                    // pending withdraw reddi -> usableBalance + amount (rezerv iade)
                    w.setUsableBalance(w.getUsableBalance().add(tx.getAmount()));
                }
            }
            default -> throw new IllegalStateException("Unsupported transaction type");
        }

        tx.setStatus(req.status());
        txs.save(tx);

        return map(tx);
    }


    // ---------- WITHDRAW ----------
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest req) {
        Wallet w = wallets.findById(req.walletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        // Ayar kontrolleri (oppositePartyType'a göre)
        switch (req.oppositePartyType()) {
            case PAYMENT -> {
                if (!w.isActiveForShopping()) {
                    throw new IllegalStateException("Wallet is not active for shopping");
                }
            }
            case IBAN -> {
                if (!w.isActiveForWithdraw()) {
                    throw new IllegalStateException("Wallet is not active for withdraw");
                }
            }
        }

        // Yeterli usable balance kontrolü (hem rezerv hem direkt çekim için gerekli)
        if (w.getUsableBalance().compareTo(req.amount()) < 0) {
            throw new IllegalStateException("Insufficient usable balance");
        }

        boolean pending = req.amount().compareTo(THRESHOLD) > 0;

        // Bakiye yansımaları
        if (pending) {
            // sadece usableBalance'dan rezerv düş
            w.setUsableBalance(w.getUsableBalance().subtract(req.amount()));
        } else {
            // onaylı çekim: usableBalance ve balance'dan düş
            w.setUsableBalance(w.getUsableBalance().subtract(req.amount()));
            w.setBalance(w.getBalance().subtract(req.amount()));
        }

        // Transaction kaydı
        Transaction tx = new Transaction();
        tx.setWallet(w);
        tx.setType(TransactionType.WITHDRAW);
        tx.setAmount(req.amount());
        tx.setOppositePartyType(req.oppositePartyType());
        tx.setOppositeParty(req.destination());
        tx.setStatus(pending ? TransactionStatus.PENDING : TransactionStatus.APPROVED);

        Transaction saved = txs.save(tx);
        return map(saved);
    }

    // mapper
    private TransactionResponse map(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getWallet().getId(),
                t.getAmount(),
                t.getType(),
                t.getOppositePartyType(),
                t.getOppositeParty(),
                t.getStatus(),
                t.getCreatedAt()
        );
    }

}

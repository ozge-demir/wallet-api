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

    // DEPOSIT
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

        // balance updates
        if (pending) {
            // only balance increases
            w.setBalance(w.getBalance().add(req.amount()));
        } else {
            // both balance and usableBalance increase
            w.setBalance(w.getBalance().add(req.amount()));
            w.setUsableBalance(w.getUsableBalance().add(req.amount()));
        }

        // persist transaction (Wallet will be updated via JPA dirty checking)
        Transaction saved = txs.save(tx);

        return map(saved);
    }

    // LIST
    @Transactional(readOnly = true)
    public Page<TransactionResponse> list(Long walletId, Pageable p) {
        return txs.findByWalletId(walletId, p).map(this::map);
    }

    // APPROVE/DENY
    @Transactional
    public TransactionResponse approve(Long txId, ApproveRequest req) {
        Transaction tx = txs.findById(txId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        // Only ACCEPT APPROVED/DENIED
        if (req.status() == TransactionStatus.PENDING) {
            throw new IllegalArgumentException("status must be APPROVED or DENIED");
        }

        // If already finalized, return 409
        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is already finalized");
        }

        Wallet w = tx.getWallet();

        switch (tx.getType()) {
            case DEPOSIT -> {
                if (req.status() == TransactionStatus.APPROVED) {
                    // pending deposit -> add to usableBalance (balance was already increased)
                    w.setUsableBalance(w.getUsableBalance().add(tx.getAmount()));
                } else { // DENIED
                    // deny pending deposit -> subtract from balance (revert the earlier increase)
                    w.setBalance(w.getBalance().subtract(tx.getAmount()));
                }
            }
            case WITHDRAW -> {
                if (req.status() == TransactionStatus.APPROVED) {
                    // approve pending withdraw -> subtract from balance (usable was already reserved)
                    w.setBalance(w.getBalance().subtract(tx.getAmount()));
                } else { // DENIED
                    // deny pending withdraw -> add back to usableBalance (release reservation)
                    w.setUsableBalance(w.getUsableBalance().add(tx.getAmount()));
                }
            }
            default -> throw new IllegalStateException("Unsupported transaction type");
        }

        tx.setStatus(req.status());
        txs.save(tx);

        return map(tx);
    }

    // WITHDRAW
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest req) {
        Wallet w = wallets.findById(req.walletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        // Feature flags depending on oppositePartyType
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

        // Sufficient usable balance check (required for both reservation and instant withdraw)
        if (w.getUsableBalance().compareTo(req.amount()) < 0) {
            throw new IllegalStateException("Insufficient usable balance");
        }

        boolean pending = req.amount().compareTo(THRESHOLD) > 0;

        // balance updates
        if (pending) {
            // for pending: reserve only from usableBalance
            w.setUsableBalance(w.getUsableBalance().subtract(req.amount()));
        } else {
            // for approved: subtract from both usableBalance and balance
            w.setUsableBalance(w.getUsableBalance().subtract(req.amount()));
            w.setBalance(w.getBalance().subtract(req.amount()));
        }

        // persist transaction
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

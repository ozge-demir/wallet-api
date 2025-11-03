package com.ozgedemir.wallet.domain.repos;

import com.ozgedemir.wallet.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);

    // (İstersen ileride filtreli listeleme için şu imzaları da ekleyebilirsin)
    // Page<Transaction> findByWalletIdAndType(Long walletId, TransactionType type, Pageable pageable);
    // Page<Transaction> findByWalletIdAndStatus(Long walletId, TransactionStatus status, Pageable pageable);
    // Page<Transaction> findByWalletIdAndStatusAndType(Long walletId, TransactionStatus status, TransactionType type, Pageable pageable);
}

package com.ozgedemir.wallet.service;

import com.ozgedemir.wallet.domain.entities.Transaction;
import com.ozgedemir.wallet.domain.entities.Wallet;
import com.ozgedemir.wallet.domain.enums.OppositePartyType;
import com.ozgedemir.wallet.domain.enums.TransactionStatus;
import com.ozgedemir.wallet.domain.enums.TransactionType;
import com.ozgedemir.wallet.domain.repos.TransactionRepository;
import com.ozgedemir.wallet.domain.repos.WalletRepository;
import com.ozgedemir.wallet.dto.tx.ApproveRequest;
import com.ozgedemir.wallet.dto.tx.DepositRequest;
import com.ozgedemir.wallet.dto.tx.WithdrawRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    WalletRepository wallets = mock(WalletRepository.class);
    TransactionRepository txs = mock(TransactionRepository.class);
    TransactionService service = new TransactionService(wallets, txs);

    Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setActiveForShopping(true);
        wallet.setActiveForWithdraw(true);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsableBalance(BigDecimal.ZERO);
        when(wallets.findById(1L)).thenReturn(Optional.of(wallet));
        when(txs.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deposit_under_threshold_isApproved_and_adds_to_both_balances() {
        var req = new DepositRequest(1L, new BigDecimal("950"), OppositePartyType.IBAN, "TR0001");

        var res = service.deposit(req);

        assertThat(res.status()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(wallet.getBalance()).isEqualByComparingTo("950");
        assertThat(wallet.getUsableBalance()).isEqualByComparingTo("950");

        ArgumentCaptor<Transaction> cap = ArgumentCaptor.forClass(Transaction.class);
        verify(txs).save(cap.capture());
        assertThat(cap.getValue().getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void deposit_over_threshold_isPending_and_adds_only_balance() {
        var res = service.deposit(new DepositRequest(1L, new BigDecimal("1200"), OppositePartyType.IBAN, "TRX"));

        assertThat(res.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(wallet.getBalance()).isEqualByComparingTo("1200");
        assertThat(wallet.getUsableBalance()).isEqualByComparingTo("0");
    }

    @Test
    void withdraw_under_threshold_isApproved_and_deducts_from_both_balances() {
        wallet.setBalance(new BigDecimal("1000"));
        wallet.setUsableBalance(new BigDecimal("1000"));

        var res = service.withdraw(new WithdrawRequest(1L, new BigDecimal("400"), OppositePartyType.PAYMENT, "PAY1"));

        assertThat(res.status()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(wallet.getBalance()).isEqualByComparingTo("600");
        assertThat(wallet.getUsableBalance()).isEqualByComparingTo("600");
    }

    @Test
    void withdraw_over_threshold_isPending_and_deducts_only_usable() {
        wallet.setBalance(new BigDecimal("1500"));
        wallet.setUsableBalance(new BigDecimal("1500"));

        var res = service.withdraw(new WithdrawRequest(1L, new BigDecimal("1200"), OppositePartyType.PAYMENT, "PAY1"));

        assertThat(res.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(wallet.getUsableBalance()).isEqualByComparingTo("300"); // düşüldü
        assertThat(wallet.getBalance()).isEqualByComparingTo("1500");     // değişmedi
    }

    @Test
    void withdraw_shouldFail_when_withdrawDisabled() {
        wallet.setActiveForWithdraw(false);
        wallet.setBalance(new BigDecimal("100"));
        wallet.setUsableBalance(new BigDecimal("100"));

        assertThatThrownBy(() ->
                service.withdraw(new WithdrawRequest(1L, new BigDecimal("10"), OppositePartyType.IBAN, "TR1"))
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Wallet is not active for withdraw");
    }



    @Test
    void approve_pending_deposit_moves_amount_to_usableBalance() {
        // pending deposit senaryosu
        var tx = new Transaction();
        tx.setId(10L);
        tx.setWallet(wallet);
        tx.setType(TransactionType.DEPOSIT);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setAmount(new BigDecimal("1200"));
        wallet.setBalance(new BigDecimal("1200"));
        wallet.setUsableBalance(BigDecimal.ZERO);

        when(txs.findById(10L)).thenReturn(Optional.of(tx));

        var res = service.approve(10L, new ApproveRequest(TransactionStatus.APPROVED));

        assertThat(res.status()).isEqualTo(TransactionStatus.APPROVED);
        assertThat(wallet.getUsableBalance()).isEqualByComparingTo("1200"); // usable eklendi
        assertThat(wallet.getBalance()).isEqualByComparingTo("1200");       // zaten ekliydi
    }

    @Test
    void approve_deny_pending_withdraw_puts_back_to_usable() {
        // pending withdraw senaryosu
        var tx = new Transaction();
        tx.setId(11L);
        tx.setWallet(wallet);
        tx.setType(TransactionType.WITHDRAW);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setAmount(new BigDecimal("1300"));
        wallet.setBalance(new BigDecimal("5000"));
        wallet.setUsableBalance(new BigDecimal("3700")); // 5000-1300 önce düşmüştü varsayalım

        when(txs.findById(11L)).thenReturn(Optional.of(tx));

        var res = service.approve(11L, new ApproveRequest(TransactionStatus.DENIED));

        assertThat(res.status()).isEqualTo(TransactionStatus.DENIED);
        assertThat(wallet.getUsableBalance()).isEqualByComparingTo("5000"); // geri koyuldu
        assertThat(wallet.getBalance()).isEqualByComparingTo("5000");       // değişmedi
    }
}

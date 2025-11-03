package com.ozgedemir.wallet.controller;

import com.ozgedemir.wallet.domain.enums.Currency;
import com.ozgedemir.wallet.dto.wallet.CreateWalletRequest;
import com.ozgedemir.wallet.dto.wallet.WalletResponse;
import com.ozgedemir.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Wallets", description = "Create & list wallets")
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService service) {
        this.walletService = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody CreateWalletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.create(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','CUSTOMER')")
    public ResponseEntity<List<WalletResponse>> list(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Currency currency
    ) {
        return ResponseEntity.ok(walletService.list(customerId, currency));
    }



}
package com.ozgedemir.wallet.controller;

import com.ozgedemir.wallet.dto.tx.*;
import com.ozgedemir.wallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transactions", description = "Deposit, withdraw, approve/deny and list")
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService txService;
    public TransactionController(TransactionService s) { this.txService = s; }

    // 1) Deposit
    @Operation(summary = "Deposit", description = "≤1000 APPROVED, >1000 PENDING")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/deposits")
    @PreAuthorize("hasAnyRole('EMPLOYEE','CUSTOMER')")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(txService.deposit(req));
    }

    // 2) List
    @Operation(summary = "List transactions")
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','CUSTOMER')")
    public Page<TransactionResponse> list(
            @RequestParam Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return txService.list(walletId, PageRequest.of(page, size));
    }

    // 3) Withdraw
    @Operation(summary = "Withdraw", description = "≤1000 APPROVED, >1000 PENDING. Flag & usable checks apply.")
    @PostMapping("/withdrawals")
    @PreAuthorize("hasAnyRole('EMPLOYEE','CUSTOMER')")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(txService.withdraw(req));
    }

    // 4) Approve/Deny
    @Operation(summary = "Approve or deny PENDING transaction")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public TransactionResponse approve(@PathVariable Long id, @Valid @RequestBody ApproveRequest req) {
        return txService.approve(id, req);
    }
}

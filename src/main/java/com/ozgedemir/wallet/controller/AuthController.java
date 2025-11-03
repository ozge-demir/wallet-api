package com.ozgedemir.wallet.controller;

import com.ozgedemir.wallet.dto.auth.LoginRequest;
import com.ozgedemir.wallet.dto.auth.LoginResponse;
import com.ozgedemir.wallet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        String token = auth.login(req.username(), req.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}


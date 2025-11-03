package com.ozgedemir.wallet.service;

import com.ozgedemir.wallet.domain.entities.Customer;
import com.ozgedemir.wallet.domain.repos.CustomerRepository;
import com.ozgedemir.wallet.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    public AuthService(CustomerRepository customers, PasswordEncoder encoder, JwtUtil jwt) {
        this.customers = customers; this.encoder = encoder; this.jwt = jwt;
    }

    public String login(String username, String password) {
        Customer c = customers.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(password, c.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        return jwt.generateToken(c.getUsername(), c.getRole());
    }
}


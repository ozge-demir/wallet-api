package com.ozgedemir.wallet.security;

import com.ozgedemir.wallet.controller.WalletController;
import com.ozgedemir.wallet.service.WalletService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;   // <- DOĞRU İMPORT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@Import({ SecurityConfig.class, WalletControllerSecurityTest.TestBeans.class })
class WalletControllerSecurityTest {

    @Autowired MockMvc mvc;

    @TestConfiguration
    static class TestBeans {
        @Bean
        JwtAuthFilter jwtAuthFilter() {
            // No-op filter: won't authorize; expect 401 status.
            return new JwtAuthFilter(null) {
                @Override
                protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                        throws java.io.IOException, jakarta.servlet.ServletException {
                    chain.doFilter(req, res);
                }
            };
        }

        @Bean
        WalletService walletService() {
            return new WalletService(null, null) { };
        }
    }

    @Test
    void listWallets_requiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/wallets").param("customerId", "1"))
                .andExpect(status().isUnauthorized());
    }
}

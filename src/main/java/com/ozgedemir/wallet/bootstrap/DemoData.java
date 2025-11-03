package com.ozgedemir.wallet.bootstrap;

import com.ozgedemir.wallet.domain.entities.Customer;
import com.ozgedemir.wallet.domain.repos.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seedUsers(CustomerRepository customers, PasswordEncoder encoder) {
        return args -> {
            if (customers.findByUsername("employee@wallet").isEmpty()) {
                var e = new Customer();
                e.setName("Emp"); e.setSurname("Loyee");
                e.setTckn("11111111111");
                e.setUsername("employee@wallet");
                e.setPasswordHash(encoder.encode("password"));
                e.setRole("EMPLOYEE");
                customers.save(e);
            }
            if (customers.findByUsername("alice@wallet").isEmpty()) {
                var c = new Customer();
                c.setName("Alice"); c.setSurname("Customer");
                c.setTckn("22222222222");
                c.setUsername("alice@wallet");
                c.setPasswordHash(encoder.encode("password"));
                c.setRole("CUSTOMER");
                customers.save(c);
            }
        };
    }
}

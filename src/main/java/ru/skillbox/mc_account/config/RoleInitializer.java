package ru.skillbox.mc_account.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.skillbox.mc_account.model.Account;
import ru.skillbox.mc_account.model.Role;
import ru.skillbox.mc_account.repository.AccountRepository;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        createAdminUser();
        createRegularUser();
    }

    private void createAdminUser() {
        if (!accountRepository.findByEmail("admin@example.com").isPresent()) {
            Account admin = new Account();
            admin.setId(UUID.randomUUID());
            admin.setFirstName("Admin");
            admin.setLastName("Adminov");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("adminpassword"));
            admin.setRole(Role.ADMIN);
            admin.setRegDate(Instant.now());
            admin.setCreatedOn(Instant.now());

            accountRepository.save(admin);
        }
    }

    private void createRegularUser() {
        if (!accountRepository.findByEmail("user@example.com").isPresent()) {
            Account user = new Account();
            user.setId(UUID.randomUUID());
            user.setFirstName("User");
            user.setLastName("Userov");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("userpassword"));
            user.setRole(Role.USER);
            user.setRegDate(Instant.now());
            user.setCreatedOn(Instant.now());

            accountRepository.save(user);
        }
    }
}


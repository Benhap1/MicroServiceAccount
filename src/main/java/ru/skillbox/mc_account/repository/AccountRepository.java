package ru.skillbox.mc_account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillbox.mc_account.model.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);

}


package ru.skillbox.mc_account.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import ru.skillbox.common.events.account.UserEvent;
import ru.skillbox.mc_account.DTO.AccountDataDTO;
import ru.skillbox.mc_account.DTO.AccountMeDTO;
import ru.skillbox.mc_account.DTO.AccountResponseDTO;
import ru.skillbox.mc_account.exception.InvalidInputException;
import ru.skillbox.mc_account.exception.ResourceNotFoundException;
import ru.skillbox.mc_account.mapper.AccountMapper;
import ru.skillbox.mc_account.model.Account;
import ru.skillbox.mc_account.repository.AccountRepository;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    private final PasswordEncoder passwordEncoder;

    private final KafkaProducerService kafkaProducerService;


    // POST /api/v1/account
    @Validated
    public AccountMeDTO createAccount(@Valid AccountMeDTO accountMeDTO) {
        Account account = accountMapper.toEntity(accountMeDTO);
        account.setCreatedOn(Instant.now());
        account.setPassword(passwordEncoder.encode(accountMeDTO.getPassword()));
        Account savedAccount = accountRepository.save(account);
        kafkaProducerService.sendUserEvent(accountMapper.toUserEvent(savedAccount));
        return accountMapper.toDTO(savedAccount);
    }


    // GET /api/v1/account
    public AccountResponseDTO getAccount(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidInputException("Email must not be empty");
        }
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        return accountOptional.map(accountMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for email: " + email));
    }


    // PUT /api/v1/account
    @Validated
    public AccountMeDTO updateAccount(@Valid AccountMeDTO accountMeDTO) {

        UUID accountId = accountMeDTO.getId();

        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        Account account = accountOptional.get();
        account.setFirstName(accountMeDTO.getFirstName());
        account.setLastName(accountMeDTO.getLastName());
        account.setEmail(accountMeDTO.getEmail());

        if (!account.getPassword().equals(accountMeDTO.getPassword())) {
            account.setPassword(passwordEncoder.encode(accountMeDTO.getPassword()));
        }
        account.setPhone(accountMeDTO.getPhone());
        account.setPhoto(accountMeDTO.getPhoto());
        account.setProfileCover(accountMeDTO.getProfileCover());
        account.setAbout(accountMeDTO.getAbout());
        account.setCity(accountMeDTO.getCity());
        account.setCountry(accountMeDTO.getCountry());
        account.setStatusCode(accountMeDTO.getStatusCode());
        account.setRegDate(parseInstant(accountMeDTO.getRegDate()));
        account.setBirthDate(parseInstant(accountMeDTO.getBirthDate()));
        account.setMessagePermission(accountMeDTO.getMessagePermission());
        account.setLastOnlineTime(parseInstant(accountMeDTO.getLastOnlineTime()));
        account.setEmojiStatus(accountMeDTO.getEmojiStatus());
        account.setCreatedOn(parseInstant(accountMeDTO.getCreatedOn()));
        account.setUpdatedOn(Instant.now());
        account.setDeletionTimestamp(parseInstant(accountMeDTO.getDeletionTimestamp()));
        account.setDeleted(accountMeDTO.isDeleted());
        account.setOnline(accountMeDTO.isOnline());
        account.setBlocked(accountMeDTO.isBlocked());

        Account updatedAccount = accountRepository.save(account);
        kafkaProducerService.sendUserEvent(accountMapper.toUserEvent(updatedAccount));
        return accountMapper.toDTO(updatedAccount);
    }

    private Instant parseInstant(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(dateTime);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format: " + dateTime, e);
        }
    }

    // GET /api/v1/account/me
    public AccountMeDTO getAccountMe(String email) {
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        return accountOptional.map(accountMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }


    // PUT /api/v1/account/me
    public AccountMeDTO updateAccountMe(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        UserEvent userEvent = accountMapper.toUserEvent(account);
        kafkaProducerService.sendUserEvent(userEvent);
        return accountMapper.toDTO(account);
    }



    // DELETE /api/v1/account/me
    public void markAccountAsDeleted(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        account.setDeleted(true);
        account.setDeletionTimestamp(Instant.now());
        accountRepository.save(account);
        kafkaProducerService.sendUserEvent(accountMapper.toUserEvent(account));
    }


    // GET /api/v1/account/{id}
    public AccountDataDTO getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found for this account. Please assign a role.");
        }
        return accountMapper.toAccountDetailsDTOWithAuthorities(account);
    }


    // DELETE /api/v1/account/{id}
    public void markAccountAsDeleted(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        account.setDeleted(true);
        account.setDeletionTimestamp(Instant.now());
        accountRepository.save(account);
        kafkaProducerService.sendUserEvent(accountMapper.toUserEvent(account));
    }
}

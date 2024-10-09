package ru.skillbox.mc_account.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import ru.skillbox.mc_account.DTO.AccountDataDTO;
import ru.skillbox.mc_account.DTO.AccountMeDTO;
import ru.skillbox.mc_account.DTO.AccountResponseDTO;
import ru.skillbox.mc_account.config.AuthFilter;
import ru.skillbox.mc_account.exception.InvalidInputException;
import ru.skillbox.mc_account.exception.ResourceNotFoundException;
import ru.skillbox.mc_account.mapper.AccountMapper;
import ru.skillbox.mc_account.model.Account;
import ru.skillbox.mc_account.repository.AccountRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;


    // POST /api/v1/account
    @Validated
    public AccountMeDTO createAccount(@Valid AccountMeDTO accountMeDTO) {
        Account account = accountMapper.toEntity(accountMeDTO);
        account.setCreatedOn(Instant.now());
        return accountMapper.toDTO(accountRepository.save(account));
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
        account.setPassword(accountMeDTO.getPassword());
        account.setPhone(accountMeDTO.getPhone());
        account.setPhoto(accountMeDTO.getPhoto());
        account.setProfileCover(accountMeDTO.getProfileCover());
        account.setAbout(accountMeDTO.getAbout());
        account.setCity(accountMeDTO.getCity());
        account.setCountry(accountMeDTO.getCountry());
        account.setStatusCode(accountMeDTO.getStatusCode());
        account.setRegDate(accountMeDTO.getRegDate());
        account.setBirthDate(accountMeDTO.getBirthDate());
        account.setMessagePermission(accountMeDTO.getMessagePermission());
        account.setLastOnlineTime(accountMeDTO.getLastOnlineTime());
        account.setEmojiStatus(accountMeDTO.getEmojiStatus());
        account.setCreatedOn(accountMeDTO.getCreatedOn());
        account.setUpdatedOn(Instant.now());
        account.setDeletionTimestamp(accountMeDTO.getDeletionTimestamp());
        account.setDeleted(accountMeDTO.isDeleted());
        account.setOnline(accountMeDTO.isOnline());
        account.setBlocked(accountMeDTO.isBlocked());

        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toDTO(updatedAccount);
    }


    // GET /api/v1/account/me
    public AccountMeDTO getAccountMe() {
        String email = extractEmailFromCurrentToken();
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        return accountOptional.map(accountMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }



    // PUT /api/v1/account/me
    public AccountMeDTO updateAccountMe(AccountMeDTO accountMeDTO) {
        String email = extractEmailFromCurrentToken();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        account.setFirstName(accountMeDTO.getFirstName());
        account.setLastName(accountMeDTO.getLastName());
        account.setEmail(accountMeDTO.getEmail());
        account.setPassword(accountMeDTO.getPassword());
        account.setPhone(accountMeDTO.getPhone());
        account.setPhoto(accountMeDTO.getPhoto());
        account.setProfileCover(accountMeDTO.getProfileCover());
        account.setAbout(accountMeDTO.getAbout());
        account.setCity(accountMeDTO.getCity());
        account.setCountry(accountMeDTO.getCountry());
        account.setStatusCode(accountMeDTO.getStatusCode());
        account.setRegDate(accountMeDTO.getRegDate());
        account.setBirthDate(accountMeDTO.getBirthDate());
        account.setMessagePermission(accountMeDTO.getMessagePermission());
        account.setLastOnlineTime(accountMeDTO.getLastOnlineTime());
        account.setEmojiStatus(accountMeDTO.getEmojiStatus());
        account.setUpdatedOn(Instant.now());

        accountRepository.save(account);
        return accountMapper.toDTO(account);
    }


    // DELETE /api/v1/account/me
    public void deleteAccountMe() {

        String email = extractEmailFromCurrentToken();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        accountRepository.delete(account);
    }


    // GET /api/v1/account/me
    private String extractEmailFromCurrentToken() {

        String token = AuthFilter.getCurrentToken();
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidInputException("Authorization token must not be empty");
        }

        String email;
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                throw new InvalidInputException("Invalid token format: Token must have 3 parts");
            }
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(payload);
            if (!jsonObject.has("email")) {
                throw new InvalidInputException("Email is missing from the token");
            }

            email = jsonObject.getString("email");
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid token format: Unable to decode payload");
        } catch (JSONException e) {
            throw new InvalidInputException("Invalid token format: Error parsing payload");
        } catch (Exception e) {
            throw new InvalidInputException("Invalid token format: " + e.getMessage());
        }

        return email;
    }

    // GET /api/v1/account/{id}
    public AccountDataDTO getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found for this account");
        }
        return accountMapper.toAccountDetailsDTOWithAuthorities(account);
    }


    // DELETE /api/v1/account/{id}
    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        accountRepository.deleteById(id);
    }
}

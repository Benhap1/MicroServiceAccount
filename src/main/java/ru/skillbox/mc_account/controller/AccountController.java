package ru.skillbox.mc_account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.mc_account.DTO.AccountDataDTO;
import ru.skillbox.mc_account.DTO.AccountMeDTO;
import ru.skillbox.mc_account.DTO.AccountResponseDTO;
import ru.skillbox.mc_account.service.AccountService;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);


    @Operation(summary = "Создание нового аккаунта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Аккаунт успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    @PostMapping
    public ResponseEntity<AccountMeDTO> createAccount(@Valid @RequestBody AccountMeDTO accountMeDTO) {
        AccountMeDTO createdAccount = accountService.createAccount(accountMeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }


    @Operation(
            summary = "Получение аккаунта по email",
            parameters = {
                    @Parameter(name = "email", description = "Email адрес", required = true, in = ParameterIn.QUERY)
            }
    )
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<AccountResponseDTO> getAccount(
            @RequestParam String email) {
        logger.debug("Received request with email: {}", email);
        AccountResponseDTO account = accountService.getAccount(email);
        logger.debug("Returning account: {}", account);
        return ResponseEntity.ok(account);
    }


    @Operation(summary = "Обновление аккаунта")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping
    public ResponseEntity<AccountMeDTO> updateAccount(
            @Valid @RequestBody AccountMeDTO accountMeDTO) {
        AccountMeDTO updatedAccount = accountService.updateAccount(accountMeDTO);
        return ResponseEntity.ok(updatedAccount);
    }

    // GET /api/v1/account/me
    @Operation(summary = "Получение информации о текущем аккаунте")
    @GetMapping("/me")
    public ResponseEntity<AccountMeDTO> getCurrentAccount(Authentication authentication) {
        String email = authentication.getName();
        AccountMeDTO account = accountService.getAccountMe(email);
        return ResponseEntity.ok(account);
    }

    // PUT /api/v1/account/me
    @Operation(summary = "Обновление информации о текущем аккаунте")
    @PutMapping("/me")
    public ResponseEntity<AccountMeDTO> updateAccountMe(
            Authentication authentication) {
        AccountMeDTO updatedAccount = accountService.updateAccountMe(authentication.getName());
        return ResponseEntity.ok(updatedAccount);
    }



    @Operation(summary = "Пометить текущий аккаунт как удалённый")
    @DeleteMapping("/me")
    public ResponseEntity<Void> markAccountAsDeleted(Authentication authentication) {
    String email = authentication.getName();
    accountService.markAccountAsDeleted(email);
    return ResponseEntity.ok().build();
    }


    @Operation(summary = "Получение аккаунта по ID")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AccountDataDTO> getAccountById(@PathVariable UUID id) {
        AccountDataDTO accountDTO = accountService.getAccountById(id);
        return ResponseEntity.ok(accountDTO);
    }

    @Operation(summary = "Пометить аккаунт как удалённый по ID")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> markAccountAsDeletedById(@PathVariable UUID id) {
        accountService.markAccountAsDeleted(id);
        return ResponseEntity.ok().build();
    }
}
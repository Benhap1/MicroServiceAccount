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
    @GetMapping
    public ResponseEntity<AccountResponseDTO> getAccount(
            @RequestParam String email) {
        logger.debug("Received request with email: {}", email);
        AccountResponseDTO account = accountService.getAccount(email); // Изменен тип возвращаемого значения
        logger.debug("Returning account: {}", account);
        return ResponseEntity.ok(account);
    }


    @Operation(summary = "Обновление аккаунта")
    @PutMapping
    public ResponseEntity<AccountMeDTO> updateAccount(
            @Valid @RequestBody AccountMeDTO accountMeDTO) {
        AccountMeDTO updatedAccount = accountService.updateAccount(accountMeDTO);
        return ResponseEntity.ok(updatedAccount);
    }


    @Operation(summary = "Получение информации о текущем аккаунте")
    @GetMapping("/me")
    public ResponseEntity<AccountMeDTO> getAccountMe() {
        AccountMeDTO account = accountService.getAccountMe();
        return ResponseEntity.ok(account);
    }



    @Operation(summary = "Обновление информации о текущем аккаунте")
    @PutMapping("/me")
    public ResponseEntity<AccountMeDTO> updateAccountMe(
            @RequestBody AccountMeDTO accountMeDTO) {
        AccountMeDTO updatedAccount = accountService.updateAccountMe(accountMeDTO);
        return ResponseEntity.ok(updatedAccount);
    }


    @Operation(summary = "Удаление текущего аккаунта")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccountMe() {
        accountService.deleteAccountMe();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получение аккаунта по ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountDataDTO> getAccountById(@PathVariable UUID id) {
        AccountDataDTO accountDTO = accountService.getAccountById(id);
        return ResponseEntity.ok(accountDTO);
    }

    @Operation(summary = "Удаление аккаунта по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok().build();
    }
}
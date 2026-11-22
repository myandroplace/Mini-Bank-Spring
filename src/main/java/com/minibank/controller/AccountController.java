package com.minibank.controller;

import com.minibank.dto.AccountResponse;
import com.minibank.dto.AmountOperationRequest;
import com.minibank.dto.CreateAccountRequest;
import com.minibank.dto.TotalBalanceResponse;
import com.minibank.dto.TransactionResponse;
import com.minibank.mapper.BankMapper;
import com.minibank.model.Account;
import com.minibank.model.Transaction;
import com.minibank.model.TransactionCategory;
import com.minibank.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * REST-контроллер для управления банковскими счетами, балансом и аналитикой.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Управление банковскими счетами, операциями и аналитикой")
public class AccountController {

    private final BankService bankService;
    private final BankMapper bankMapper;

    public AccountController(BankService bankService, BankMapper bankMapper) {
        this.bankService = bankService;
        this.bankMapper = bankMapper;
    }

    @PostMapping
    @Operation(summary = "Открыть новый банковский счет")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account created = bankService.createAccount(
                request.ownerName(),
                request.initialDeposit(),
                request.currency()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(bankMapper.toAccountResponse(created));
    }

    @GetMapping
    @Operation(summary = "Получить список всех банковских счетов")
    public List<AccountResponse> getAllAccounts() {
        return bankService.getAllAccounts().stream()
                .map(bankMapper::toAccountResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить данные счета по ID")
    public AccountResponse getAccount(@PathVariable Long id) {
        return bankMapper.toAccountResponse(bankService.getAccount(id));
    }

    @GetMapping("/by-number/{accountNumber}")
    @Operation(summary = "Найти счет по его номеру (например, MB-RUB-000001)")
    public AccountResponse getAccountByNumber(@PathVariable String accountNumber) {
        return bankMapper.toAccountResponse(bankService.getAccountByNumber(accountNumber));
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Пополнить банковский счет")
    public TransactionResponse deposit(@PathVariable Long id,
                                       @Valid @RequestBody AmountOperationRequest request) {
        Transaction tx = bankService.deposit(id, request.amount(), request.category(), request.description());
        return bankMapper.toTransactionResponse(tx);
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Списать средства со счета")
    public TransactionResponse withdraw(@PathVariable Long id,
                                        @Valid @RequestBody AmountOperationRequest request) {
        Transaction tx = bankService.withdraw(id, request.amount(), request.category(), request.description());
        return bankMapper.toTransactionResponse(tx);
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Заблокировать счет")
    public AccountResponse blockAccount(@PathVariable Long id) {
        bankService.blockAccount(id);
        return bankMapper.toAccountResponse(bankService.getAccount(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Разблокировать счет")
    public AccountResponse activateAccount(@PathVariable Long id) {
        bankService.activateAccount(id);
        return bankMapper.toAccountResponse(bankService.getAccount(id));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Закрыть счет (требуется нулевой баланс)")
    public AccountResponse closeAccount(@PathVariable Long id) {
        bankService.closeAccount(id);
        return bankMapper.toAccountResponse(bankService.getAccount(id));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Получить историю транзакций по счету")
    public List<TransactionResponse> getAccountTransactions(@PathVariable Long id) {
        return bankService.getAccountTransactions(id).stream()
                .map(bankMapper::toTransactionResponse)
                .toList();
    }

    @GetMapping("/{id}/analytics/expenses")
    @Operation(summary = "Аналитика расходов по категориям (Stream API)")
    public Map<TransactionCategory, BigDecimal> getExpensesByCategory(@PathVariable Long id) {
        return bankService.getExpensesByCategory(id);
    }

    @GetMapping("/total-balance")
    @Operation(summary = "Рассчитать общую ликвидность банка в указанной валюте")
    public TotalBalanceResponse getTotalBalance(@RequestParam(defaultValue = "RUB") String currency) {
        String normalizedCurrency = currency.trim().toUpperCase();
        BigDecimal total = bankService.getTotalBalance(normalizedCurrency);
        return new TotalBalanceResponse(normalizedCurrency, total);
    }
}

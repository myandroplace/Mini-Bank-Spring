package com.minibank.controller;

import com.minibank.dto.TransactionResponse;
import com.minibank.dto.TransferRequest;
import com.minibank.mapper.BankMapper;
import com.minibank.model.Transaction;
import com.minibank.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-контроллер для переводов между счетами и просмотра общего реестра транзакций.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transactions", description = "Межбанковские переводы и общий журнал транзакций")
public class TransactionController {

    private final BankService bankService;
    private final BankMapper bankMapper;

    public TransactionController(BankService bankService, BankMapper bankMapper) {
        this.bankService = bankService;
        this.bankMapper = bankMapper;
    }

    @PostMapping("/transfers")
    @Operation(summary = "Выполнить перевод средств между двумя счетами")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        Transaction tx = bankService.transfer(
                request.sourceAccountId(),
                request.targetAccountId(),
                request.amount(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(bankMapper.toTransactionResponse(tx));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Получить полный журнал всех транзакций системы")
    public List<TransactionResponse> getAllTransactions() {
        return bankService.getAllTransactions().stream()
                .map(bankMapper::toTransactionResponse)
                .toList();
    }
}

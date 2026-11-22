package com.minibank.mapper;

import com.minibank.dto.AccountResponse;
import com.minibank.dto.TransactionResponse;
import com.minibank.model.Account;
import com.minibank.model.Transaction;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования доменных сущностей в DTO ответа API.
 */
@Component
public class BankMapper {

    public AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getOwnerName(),
                account.getBalance(),
                account.getStatus(),
                account.getCurrency(),
                account.getCreatedAt()
        );
    }

    public TransactionResponse toTransactionResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getTimestamp(),
                tx.getAmount(),
                tx.getType(),
                tx.getCategory(),
                tx.getCategory().getDisplayName(),
                tx.getSourceAccountId(),
                tx.getTargetAccountId(),
                tx.getDescription()
        );
    }
}

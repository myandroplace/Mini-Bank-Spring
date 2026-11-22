package com.minibank.dto;

import com.minibank.model.TransactionCategory;
import com.minibank.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO ответа с информацией о транзакции из журнала операций.
 */
public record TransactionResponse(
        Long id,
        LocalDateTime timestamp,
        BigDecimal amount,
        TransactionType type,
        TransactionCategory category,
        String categoryDisplayName,
        Long sourceAccountId,
        Long targetAccountId,
        String description
) {
}

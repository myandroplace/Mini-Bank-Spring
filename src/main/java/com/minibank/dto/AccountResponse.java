package com.minibank.dto;

import com.minibank.model.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO ответа с данными банковского счета (изоляция доменной модели от REST-контракта).
 */
public record AccountResponse(
        Long id,
        String accountNumber,
        String ownerName,
        BigDecimal balance,
        AccountStatus status,
        String currency,
        LocalDateTime createdAt
) {
}

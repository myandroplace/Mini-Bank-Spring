package com.minibank.dto;

import java.math.BigDecimal;

/**
 * DTO ответа для агрегированной ликвидности активных счетов в заданной валюте.
 */
public record TotalBalanceResponse(
        String currency,
        BigDecimal totalBalance
) {
}

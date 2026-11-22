package com.minibank.dto;

import com.minibank.model.TransactionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO запроса для операций пополнения (deposit) и снятия средств (withdraw).
 */
public record AmountOperationRequest(
        @NotNull(message = "Сумма операции обязательна")
        @DecimalMin(value = "0.01", message = "Сумма операции должна быть строго больше нуля")
        BigDecimal amount,

        TransactionCategory category,

        @Size(max = 255, message = "Описание не должно превышать 255 символов")
        String description
) {
}

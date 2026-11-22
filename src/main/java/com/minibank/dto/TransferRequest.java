package com.minibank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO запроса на межбанковский перевод между двумя счетами.
 */
public record TransferRequest(
        @NotNull(message = "ID счета отправителя обязателен")
        Long sourceAccountId,

        @NotNull(message = "ID счета получателя обязателен")
        Long targetAccountId,

        @NotNull(message = "Сумма перевода обязательна")
        @DecimalMin(value = "0.01", message = "Сумма перевода должна быть строго больше нуля")
        BigDecimal amount,

        @Size(max = 255, message = "Описание не должно превышать 255 символов")
        String description
) {
}

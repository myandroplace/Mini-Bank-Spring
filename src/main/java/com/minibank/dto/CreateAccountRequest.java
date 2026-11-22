package com.minibank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO запроса на открытие нового банковского счета.
 */
public record CreateAccountRequest(
        @NotBlank(message = "Имя владельца обязательно")
        @Size(min = 2, max = 100, message = "Имя владельца должно содержать от 2 до 100 символов")
        String ownerName,

        @DecimalMin(value = "0.00", message = "Начальный депозит не может быть отрицательным")
        BigDecimal initialDeposit,

        @Size(min = 3, max = 3, message = "Код валюты должен состоять ровно из 3 символов (например, RUB)")
        String currency
) {
}

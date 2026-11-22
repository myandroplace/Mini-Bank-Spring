package com.minibank.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты REST API с использованием MockMvc и полного контекста Spring Boot.
 * Профиль 'test' отключает DemoDataInitializer для полной изоляции тестов.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createTestAccount(String ownerName, String initialDeposit) throws Exception {
        String body = """
                {
                  "ownerName": "%s",
                  "initialDeposit": %s,
                  "currency": "RUB"
                }
                """.formatted(ownerName, initialDeposit);

        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/accounts/")))
                .andExpect(jsonPath("$.ownerName").value(ownerName))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/v1/accounts — ошибка Bean Validation возвращает 400 и карту полей")
    void createAccount_withInvalidPayload_shouldReturn400WithValidationErrors() throws Exception {
        String invalidBody = """
                {
                  "ownerName": "",
                  "initialDeposit": -500.00,
                  "currency": "R"
                }
                """;

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.ownerName").exists())
                .andExpect(jsonPath("$.validationErrors.initialDeposit").exists())
                .andExpect(jsonPath("$.validationErrors.currency").exists());
    }

    @Test
    @DisplayName("Пополнение, списание и аналитика расходов по категориям через REST API")
    void depositWithdrawAndAnalytics_shouldUpdateBalanceAndReturnCategorySummary() throws Exception {
        long accountId = createTestAccount("Елена Морозова", "10000.00");

        mockMvc.perform(post("/api/v1/accounts/{id}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 5000.00,
                                  "category": "SALARY",
                                  "description": "Бонус"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(5000.00));

        mockMvc.perform(post("/api/v1/accounts/{id}/withdraw", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 2300.50,
                                  "category": "GROCERIES",
                                  "description": "Супермаркет"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(12699.50));

        mockMvc.perform(get("/api/v1/accounts/{id}/analytics/expenses", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.GROCERIES").value(2300.50));
    }

    @Test
    @DisplayName("Списание сверх остатка возвращает 422 Unprocessable Entity")
    void withdraw_insufficientFunds_shouldReturn422() throws Exception {
        long accountId = createTestAccount("Максим Соколов", "1000.00");

        mockMvc.perform(post("/api/v1/accounts/{id}/withdraw", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 5000.00,
                                  "category": "OTHER",
                                  "description": "Попытка овердрафта"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(containsString("Недостаточно средств")));
    }

    @Test
    @DisplayName("POST /api/v1/transfers — перевод средств и обработка блокировки (409 Conflict)")
    void transfer_happyPathAndBlockedAccount_shouldBehaveCorrectly() throws Exception {
        long senderId = createTestAccount("Отправитель API", "20000.00");
        long recipientId = createTestAccount("Получатель API", "3000.00");

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": %d,
                                  "targetAccountId": %d,
                                  "amount": 7500.00,
                                  "description": "Оплата заказа"
                                }
                                """.formatted(senderId, recipientId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.sourceAccountId").value(senderId))
                .andExpect(jsonPath("$.targetAccountId").value(recipientId));

        mockMvc.perform(get("/api/v1/accounts/{id}", senderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(12500.00));

        mockMvc.perform(get("/api/v1/accounts/{id}", recipientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(10500.00));

        mockMvc.perform(patch("/api/v1/accounts/{id}/block", senderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": %d,
                                  "targetAccountId": %d,
                                  "amount": 100.00,
                                  "description": "Перевод с заблокированного"
                                }
                                """.formatted(senderId, recipientId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} — несуществующий счет возвращает 404 Not Found")
    void getAccount_nonExistent_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/accounts/999999"));
    }
}

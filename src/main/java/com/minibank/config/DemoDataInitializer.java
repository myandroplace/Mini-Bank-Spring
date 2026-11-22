package com.minibank.config;

import com.minibank.model.Account;
import com.minibank.model.TransactionCategory;
import com.minibank.service.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Инициализатор тестовых данных при старте приложения (отключен в профиле 'test').
 * Позволяет сразу тестировать REST API через Swagger UI или Postman после запуска.
 */
@Component
@Profile("!test")
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final BankService bankService;

    public DemoDataInitializer(BankService bankService) {
        this.bankService = bankService;
    }

    @Override
    public void run(String... args) {
        Account alice = bankService.createAccount("Алиса Смирнова", new BigDecimal("150000.00"), "RUB");
        Account bob = bankService.createAccount("Боб Иванов", new BigDecimal("45000.00"), "RUB");
        Account charlie = bankService.createAccount("Чарли Кузнецов", new BigDecimal("1000.00"), "RUB");

        bankService.deposit(bob.getId(), new BigDecimal("25000.00"), TransactionCategory.SALARY, "Ежемесячная премия");
        bankService.withdraw(alice.getId(), new BigDecimal("12400.50"), TransactionCategory.GROCERIES, "Закупка продуктов");
        bankService.withdraw(alice.getId(), new BigDecimal("6500.00"), TransactionCategory.UTILITIES, "Оплата ЖКУ");
        bankService.withdraw(alice.getId(), new BigDecimal("3200.00"), TransactionCategory.ENTERTAINMENT, "Билеты в театр");
        bankService.transfer(alice.getId(), bob.getId(), new BigDecimal("20000.00"), "Возврат долга за поездку");

        log.info("Демо-данные загружены: {} активных счетов (Alice=#{}, Bob=#{}, Charlie=#{})",
                bankService.getAllAccounts().size(), alice.getId(), bob.getId(), charlie.getId());
    }
}

# Mini-Bank Spring (REST API) 🏦🍃

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![OpenAPI](https://img.shields.io/badge/Swagger%20UI-OpenAPI%203-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://springdoc.org/)

RESTful банковская система на базе **Spring Boot 3**, выросшая из консольного проекта [Mini-Bank (Java Core)](https://github.com/myandroplace/Mini-Bank).

Проект демонстрирует бесшовную эволюцию чистой многослойной архитектуры (**Clean / Layered Architecture**): благодаря соблюдению принципа инверсии зависимостей (DIP) и внедрению зависимостей через конструктор, ядро бизнес-логики (`BankServiceImpl`) перенесено в Spring IoC-контейнер без переписывания доменных правил.

---

## 🏗️ Архитектура проекта

```text
com.minibank
├── config/
│   └── DemoDataInitializer.java       # Автозаполнение демо-счетов (@Profile("!test"))
├── controller/
│   ├── AccountController.java         # REST-эндпоинты /api/v1/accounts/**
│   └── TransactionController.java     # REST-эндпоинты /api/v1/transfers, /transactions
├── dto/                               # Immutable Java record DTO + Jakarta Validation
├── mapper/
│   └── BankMapper.java                # Маппинг Domain Entity -> Response DTO
├── model/                             # Доменные сущности (Account, Transaction) и Enum
├── exception/
│   ├── ...                            # Иерархия кастомных бизнес-исключений
│   └── handler/
│       └── GlobalExceptionHandler.java  # Централизованный @RestControllerAdvice
├── repository/                        # Интерфейсы + потокобезопасные In-Memory @Repository
├── service/                           # Контракт BankService и @Service реализация
└── MiniBankSpringApplication.java     # Точка входа @SpringBootApplication
```

---

## 🎯 Ключевые инженерные решения (Этап 2: Spring Boot & Web)

1. **Spring IoC и Dependency Injection**:
   - Репозитории (`@Repository`), сервисы (`@Service`), маппер (`@Component`) и контроллеры (`@RestController`) управляются Spring-контейнером.
   - Внедрение зависимостей выполняется исключительно через конструктор.
2. **Изоляция контракта API с помощью Java `record` DTO**:
   - Входящие запросы (`CreateAccountRequest`, `AmountOperationRequest`, `TransferRequest`) защищены декларативной валидацией `@Valid` (`@NotBlank`, `@NotNull`, `@DecimalMin`, `@Size`).
   - Ответы API (`AccountResponse`, `TransactionResponse`, `ApiErrorResponse`) отделены от доменных моделей.
3. **Централизованная обработка ошибок (`@RestControllerAdvice`)**:
   - `AccountNotFoundException` → `404 Not Found`
   - `InsufficientFundsException` → `422 Unprocessable Entity`
   - `AccountBlockedException` / `IllegalStateException` → `409 Conflict`
   - `InvalidAmountException` / `MethodArgumentNotValidException` → `400 Bad Request` (с картой ошибок по каждому полю)
4. **Интерактивная документация OpenAPI 3 (Swagger UI)**:
   - Подключен `springdoc-openapi-starter-webmvc-ui`, позволяющий тестировать все эндпоинты прямо из браузера.
5. **Тестирование (Unit + Integration)**:
   - Юнит-тесты бизнес-логики `BankServiceTest` (JUnit 5).
   - Интеграционные тесты REST-слоя `AccountControllerIntegrationTest` (`@SpringBootTest` + `MockMvc`).

---

## 🌐 Эндпоинты REST API

| Метод | Путь | Описание |
| :--- | :--- | :--- |
| `POST` | `/api/v1/accounts` | Открыть новый банковский счет (`201 Created`) |
| `GET` | `/api/v1/accounts` | Получить список всех счетов |
| `GET` | `/api/v1/accounts/{id}` | Получить счет по ID |
| `GET` | `/api/v1/accounts/by-number/{number}` | Найти счет по номеру (`MB-RUB-000001`) |
| `POST` | `/api/v1/accounts/{id}/deposit` | Пополнить счет |
| `POST` | `/api/v1/accounts/{id}/withdraw` | Списать средства со счета |
| `PATCH` | `/api/v1/accounts/{id}/block` | Заблокировать счет |
| `PATCH` | `/api/v1/accounts/{id}/activate` | Разблокировать счет |
| `PATCH` | `/api/v1/accounts/{id}/close` | Закрыть счет (при нулевом балансе) |
| `GET` | `/api/v1/accounts/{id}/transactions` | Выписка операций по счету |
| `GET` | `/api/v1/accounts/{id}/analytics/expenses` | Аналитика расходов по категориям (Stream API) |
| `GET` | `/api/v1/accounts/total-balance?currency=RUB` | Общая ликвидность банка |
| `POST` | `/api/v1/transfers` | Выполнить перевод между счетами (`201 Created`) |
| `GET` | `/api/v1/transactions` | Полный журнал транзакций системы |

---

## 🛠️ Сборка и запуск

```bash
# 1. Сборка и запуск всех модульных и интеграционных тестов
mvn clean test

# 2. Запуск Spring Boot приложения (порт 8080)
mvn spring-boot:run
```

После запуска откройте интерактивный Swagger UI:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Примеры запросов (`curl`)

```bash
# Получить все счета (с предзагруженными демо-данными)
curl http://localhost:8080/api/v1/accounts

# Открыть новый счет
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerName":"Дмитрий Волков","initialDeposit":25000.00,"currency":"RUB"}'

# Выполнить перевод со счета #1 на счет #2
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{"sourceAccountId":1,"targetAccountId":2,"amount":5000.00,"description":"Оплата услуг"}'
```

---

## 🗺️ Дорожная карта (Roadmap)

- [x] **Этап 1**: Java Core, Maven, ООП, `BigDecimal`, Business Exceptions, In-Memory Repositories, JUnit 5 ([myandroplace/Mini-Bank](https://github.com/myandroplace/Mini-Bank))
- [x] **Этап 2 (Текущий)**: Spring Boot 3, IoC (`@Service`, `@Repository`), REST API (`@RestController`), Bean Validation (`@Valid`), `@RestControllerAdvice`, Swagger UI, `MockMvc` интеграционные тесты
- [ ] **Этап 3 (Следующий)**: PostgreSQL + Spring Data JPA (`@Entity`, `@Transactional`), миграции Liquibase/Flyway и Testcontainers

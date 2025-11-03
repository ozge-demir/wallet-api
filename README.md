# Wallet API

A Spring Boot (Java 17) backend that manages **customers, wallets, and transactions** (deposit / withdraw + approval) with **JWT authentication**, **H2 in-memory** DB, and **Flyway** migrations.

---

## Tech Stack

- Java 17, Spring Boot 3.5
- Spring Web, Spring Data JPA, Spring Security (JWT)
- H2 (in-memory), Flyway
- Jakarta Validation, Lombok
- Build: Gradle

---

## Getting Started

### Prerequisites
- **Java 17+**
- **Gradle** (or use the wrapper `./gradlew`)

### Run locally

```bash
./gradlew clean bootRun
```

#### App starts on http://localhost:8080


### Build a jar
```bash
./gradlew clean build
java -jar build/libs/wallet-api-*.jar
```

---
## Database

- DB: H2 (in-memory)
- JDBC URL: jdbc:h2:mem:walletdb
- User: sa (no password) Password: sa
- Console: http://localhost:8080/h2
(Set JDBC URL to jdbc:h2:mem:walletdb)

Schema is created by **Flyway** from `src/main/resources/db/migration`.

Demo users are seeded by com.ozgedemir.wallet.bootstrap.DemoData on startup.

---

### Authentication (JWT)

This API uses JWT Bearer tokens.

#### Seed users: 

| Username          | Password   | Role       |
| ----------------- | ---------- | ---------- |
| `employee@wallet` | `password` | `EMPLOYEE` |
| `alice@wallet`    | `password` | `CUSTOMER` |


Authorization is enforced via **SecurityConfig** and **@PreAuthorize**.
In this case study, EMPLOYEE can hit the admin endpoints.

---

## cURL Quick Start

### 1) Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
-H 'Content-Type: application/json' \
-d '{"username":"employee@wallet","password":"password"}' | jq -r .token)
```

### 2) Create a wallet
```bash
curl -i -X POST http://localhost:8080/api/v1/wallets \
-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
-d '{"customerId":1,"walletName":"Main","currency":"TRY","activeForShopping":true,"activeForWithdraw":true}'
```

### 3) Deposit
```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/deposits \
-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
-d '{"walletId":1,"amount":950,"oppositePartyType":"IBAN","source":"TR0001"}' | jq
```

### 4) Withdraw
```bash
curl -s -X POST http://localhost:8080/api/v1/transactions/withdrawals \
-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
-d '{"walletId":1,"amount":400,"oppositePartyType":"PAYMENT","destination":"PAY123"}' | jq
```

### 5) List transactions
```bash
curl -s "http://localhost:8080/api/v1/transactions?walletId=1" \
-H "Authorization: Bearer $TOKEN" | jq
```

### 6) Approve/Deny (example id=5)
```bash
curl -s -X POST "http://localhost:8080/api/v1/transactions/5/approve" \
-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
-d '{"status":"APPROVED"}' | jq
```
---

## Postman
- Please find postman collection of the endpoints under wallet-api/postman/[Wallet API.postman_collection.json](postman/Wallet%20API.postman_collection.json)
- Go Postman > Import > postman/Wallet-API.postman_collection.json

---

## Run all tests
```bash
./gradlew test
```

### Clean then run tests (fresh build)
```bash
./gradlew clean test
```

### Run a specific test class 
```bash
./gradlew test --tests "com.ozgedemir.wallet.service.TransactionServiceTest"
```

### Run a single test method
```bash
./gradlew test --tests "com.ozgedemir.wallet.service.TransactionServiceTest.withdraw_shouldFail_when_withdrawDisabled"

```
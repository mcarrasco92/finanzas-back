# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./mvnw spring-boot:run        # Run the application
./mvnw clean install          # Build (skip tests: -DskipTests)
./mvnw test                   # Run all tests
./mvnw test -Dtest=ClassName  # Run a single test class
```

The active Spring profile is `dev` (set in `application.properties`). Firebase credentials must be present at `src/main/resources/keys/firebase-dev.json` to start the application.

## Architecture

Spring Boot 3.5.1 / Java 21 REST API with a standard layered structure:

```
controller/ → service/ → repositories/ → Google Cloud Firestore (NoSQL)
```

**DTOs** in `dto/` are used for API request/response shapes. **Models** in `model/` represent Firestore document structures. All API responses are wrapped in `GenericResponse` (in `dto/`) with fields `coderr`, `message`, and `data`.

### Authentication Flow

1. Client registers/logs in via Firebase, gets a Firebase ID token.
2. Client calls `/api/users/create-token` with the Firebase token → backend validates it and returns a custom JWT.
3. All subsequent requests include `Authorization: Bearer {jwt}` header.
4. `JwtInterceptor` validates the token and extracts the `uid`, which controllers use to scope Firestore queries.

Public routes (no JWT required): `/api/users/register`, `/api/users/create-token`, `/api/users/validate-token`.

### Firestore Data Model

Data is stored per-user under `/users/{uid}/` with subcollections:
- `cuentas` — bank accounts
- `transacciones` — transactions
- `tarjetas` — credit cards
- `categorias` — income/expense categories
- `transaccionesRecurrentes` — recurring transaction rules
- `transferencias` — transfers between accounts/cards
- `msi` — installment payment plans

### Key Modules

| Feature | Controller | Notes |
|---|---|---|
| Accounts | `CuentasController` | Tracks invested/available/total balances |
| Transactions | `TransaccionesController` | Supports filtering via `TransaccionFiltroDto` |
| Credit Cards | `TarjetasController` | Balance tracked per card |
| Categories | `CategoriasController` | Used to classify transactions |
| Recurring Transactions | `TransaccionRecurrenteController` | `TransaccionRecurrenteJob` runs daily via `@Scheduled` cron |
| Transfers | `TransferenciasController` | Handles account-to-account and account-to-card |
| Installments (MSI) | `MsiController` | Multi-installment payment plan tracking |
| Profile | `PerfilController` | User profile updates |

### CORS

Configured to allow `http://localhost:4200` (Angular frontend). See `config/Cors.java`.

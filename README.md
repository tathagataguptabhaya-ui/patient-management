# Patient Management System

A microservices-based patient management system built with **Spring Boot**, using **REST** for client-facing APIs and **gRPC** for internal service-to-service communication.

## Overview

The system is split into two independent Spring Boot services:

| Service | Port (HTTP) | Port (gRPC) | Purpose |
|---|---|---|---|
| `patient-service` | 4000 | — (gRPC client) | Manages patient records (CRUD) via a REST API |
| `billing_service` | 4001 | 9001 | Creates billing accounts for patients, exposed only over gRPC |

When a new patient is created via `patient-service`, it calls `billing_service` over gRPC to automatically provision a billing account for that patient.

## Architecture

```
Client (Postman/HTTP)
        │
        ▼
┌─────────────────┐        gRPC        ┌──────────────────┐
│  patient-service │ ──────────────────▶│  billing_service  │
│   (REST, :4000)  │  createBillingAcc  │  (gRPC, :9001)    │
└─────────────────┘                    └──────────────────┘
        │
        ▼
   H2 / PostgreSQL
```

- **Communication:** `patient-service` acts as a gRPC client and `billing_service` as a gRPC server. The contract is defined in a shared `.proto` file (`billing_service.proto`), present in both modules.
- **Persistence:** `patient-service` uses Spring Data JPA with a `Patient` JPA entity, backed by PostgreSQL in production and H2 for local/testing use. `data.sql` seeds the database with sample patients on startup.
- **Validation:** Request DTOs use Bean Validation (`jakarta.validation`), with a separate validation group (`CreatePatientValidationGroup`) so that `registeredDate` is required only on creation, not on update.
- **API docs:** `patient-service` includes springdoc-openapi (Swagger UI) for interactive API exploration.
- **Security:** `patient-service` uses Spring Security with HTTP Basic auth for all endpoints except the H2 console.

## Tech Stack

- **Java 25**, **Spring Boot** (4.0.x / 4.1.x)
- **Spring Web / Spring Data JPA / Spring Validation / Spring Security**
- **gRPC** (`grpc-netty-shaded`, `grpc-protobuf`, `grpc-stub`) with `grpc-spring-boot-starter`
- **Protocol Buffers** (`protobuf-maven-plugin` for code generation)
- **PostgreSQL** (runtime) / **H2** (test/local)
- **Lombok**
- **springdoc-openapi** (Swagger UI)
- **Maven** (each service is built and managed independently with its own `pom.xml`)
- **Docker** (each service ships with its own multi-stage `Dockerfile`)

## Project Structure

```
patient-management/
├── patient-service/
│   ├── src/main/java/org/example/patientservice/
│   │   ├── controller/      # PatientController (REST endpoints)
│   │   ├── service/         # PatientService (business logic)
│   │   ├── repo/            # PatientRepository (Spring Data JPA)
│   │   ├── model/           # Patient entity
│   │   ├── dto/             # Request/Response DTOs + validation groups
│   │   ├── mapper/          # PatientMapper (entity <-> DTO)
│   │   ├── grpc/            # BilingServiceGRPCClient (gRPC client to billing_service)
│   │   ├── exeption/        # Custom exceptions + global exception handler
│   │   └── config/          # SecurityConfig
│   ├── src/main/proto/      # billing_service.proto (shared contract)
│   ├── src/main/resources/  # application.properties, data.sql
│   └── Dockerfile
├── billing_service/
│   ├── src/main/java/org/example/billing_service/
│   │   └── grpc/            # BillingGRPCSService (gRPC server implementation)
│   ├── src/main/proto/      # billing_service.proto
│   ├── src/main/resources/  # application.properties
│   └── Dockerfile
├── api-requests/patient-service/   # .http files for testing REST endpoints
└── grpc-requests/billing_service/  # .http file for testing the gRPC endpoint
```

## API Endpoints (`patient-service`)

Base URL: `http://localhost:4000/patients`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/patients` | List all patients |
| `POST` | `/patients` | Create a new patient (also provisions a billing account via gRPC) |
| `PUT` | `/patients/{id}` | Update an existing patient |
| `DELETE` | `/patients/{id}` | Delete a patient |

All endpoints require HTTP Basic authentication (except the H2 console, when enabled).

Sample request/response files are available in `api-requests/patient-service/` (`.http` files, usable directly in IntelliJ or VS Code's REST client).

## gRPC Contract

Defined in `billing_service.proto` (present in both services):

```proto
service BillingService {
  rpc CreateBillingAccount (BillingRequest) returns (BillingResponse);
}

message BillingRequest {
  string patientId = 1;
  string name = 2;
  string email = 3;
}

message BillingResponse {
  string accountId = 1;
  string status = 2;
}
```

A sample gRPC request is available in `grpc-requests/billing_service/create-billing-account.http`.

## Running Locally

Each service can be built and run independently with Maven:

```bash
# Billing service (start first, since patient-service depends on it)
cd billing_service
./mvnw spring-boot:run

# Patient service
cd patient-service
./mvnw spring-boot:run
```

- `patient-service` will be available at `http://localhost:4000`
- `billing_service` exposes gRPC on port `9001` and HTTP on port `4001`

### Configuration

`patient-service` connects to `billing_service`'s gRPC server using the properties:

```properties
billing.service.address=localhost
billing.service.grpc.port=9001
```

### Database

- **Production:** PostgreSQL (JDBC driver included as a runtime dependency; connection details need to be supplied via `application.properties` or environment variables).
- **Local/testing:** H2 in-memory database with sample data auto-loaded from `data.sql`.

## Running with Docker

Each service has its own multi-stage Dockerfile (Maven build stage → JRE runtime stage):

```bash
# Build and run billing_service
cd billing_service
docker build -t billing-service .
docker run -p 4001:4001 -p 9001:9001 billing-service

# Build and run patient-service
cd patient-service
docker build -t patient-service .
docker run -p 4000:4000 patient-service
```

## Notes

- This project is under active development — some scaffolding (e.g. commented-out H2/security properties, in-memory defaults) is present for local iteration and may change.
- The `billing_service.proto` file is duplicated in both modules rather than shared from a common module; keep both in sync if the contract changes.

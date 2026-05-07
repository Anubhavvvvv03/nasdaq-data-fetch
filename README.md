# Nasdaq Stock Microservice

A production-grade Spring Boot microservice designed to ingest, process, and serve NASDAQ symbol data with a focus on performance, scalability, and standardized error handling.

## Overview

This microservice provides a high-performance REST API for looking up stock symbols. It features automated data ingestion from CSV files, integrated caching using Redis/Valkey, and a robust, industry-standard exception handling mechanism based on RFC 7807 (Problem Details).

---

## Architecture & Flow

### 1. Data Ingestion Flow
On application startup, the `DataLoader` component (implementing `CommandLineRunner`) performs the following:
*   Locates the `nasdaq-listed-symbols.csv` in the classpath.
*   Parses the CSV using `Apache Commons CSV`.
*   Batch-saves the stock data (`symbol`, `companyName`, `securityName`) into the **PostgreSQL** database.

### 2. Request Processing Flow
When a user calls the `POST /api/v1/stock` endpoint:
1.  **Traceability**: The `TimeTrackingFilter` generates a unique `traceId` and adds it to the `MDC` (Mapped Diagnostic Context) for logging and to the request attributes for response correlation. It also records the `startTime`.
2.  **Validation**: The request body (`StockRequest`) is validated using Jakarta Bean Validation. If invalid, a `400 Bad Request` is returned immediately via the `GlobalExceptionHandler`.
3.  **Service Layer**: The `StockService` is invoked.
4.  **Caching**: The system first checks the **Valkey** cache using the `@Cacheable` annotation.
    *   **Cache Hit**: Data is returned immediately.
    *   **Cache Miss**: Data is fetched from PostgreSQL, stored in Redis for future requests, and then returned.
5.  **Response Construction**: The controller calculates the total time taken and returns a `StockResponse` containing the stock details and latency metadata.

---

## Standardized Exception Handling (SOP Implementation)

The application follows a strict Standard Operating Procedure (SOP) for exception handling to ensure consistent and secure error responses.

### 1. Exception Hierarchy
We have implemented a dual-base exception hierarchy:
*   **`BusinessException` (4xx)**: Abstract base for expected failures like "Resource Not Found" or "Validation Failed".
*   **`InfrastructureException` (5xx)**: Abstract base for technical failures like database timeouts or external service unavailability.

### 2. Error Response Contract (RFC 7807)
Every error response follows the **Problem Details** standard. Example error response:

```json
{
  "type": "about:blank",
  "title": "ResourceNotFoundException",
  "status": 404,
  "detail": "Stock with symbol AAPL123 not found",
  "instance": "/api/v1/stock",
  "errorCode": "STOCK_NOT_FOUND",
  "traceId": "a1b2c3d4-e5f6-4789-abcd-1234567890ab",
  "timestamp": "2026-05-05T12:00:00.000Z"
}
```

### 3. Key Features of Error Handling:
*   **Centralized Control**: Managed via `@RestControllerAdvice` in `GlobalExceptionHandler`.
*   **Security**: Internal stack traces are never exposed in responses. In production, generic messages are shown for 500 errors.
*   **Traceability**: Every error includes a `traceId` that corresponds to server logs, enabling instant correlation of client-side errors with backend issues.
*   **Machine-Readable Codes**: Includes an `errorCode` enum (e.g., `VALIDATION_FAILED`) for frontend logic.

---

## Technology Stack
*   **Core**: Java 17, Spring Boot 4.0.6
*   **Data**: Spring Data JPA, PostgreSQL
*   **Cache**: Spring Data Redis (Valkey compatible)
*   **Validation**: Jakarta Bean Validation
*   **Parsing**: Apache Commons CSV

---

## Configuration & Setup

### Requirements
*   JDK 17
*   Docker & Docker Compose (for PostgreSQL and Valkey)

### Running the Application
1.  **Start Infrastructure**:
    ```bash
    docker-compose up -d
    ```
2.  **Build and Run**:
    ```bash
    ./mvnw spring-boot:run
    ```

---

## Containerization & Orchestration

### 1. Docker
The application is fully dockerized. To build the image:
```bash
docker build -t nasdaq-stock-service .
```

### 2. Kubernetes (K8s) Deployment
Standardized Kubernetes manifests are provided in the `k8s/` directory for deploying the full stack (App + Database + Cache).

#### Deployment Steps:
1.  **Deploy PostgreSQL**:
    ```bash
    kubectl apply -f k8s/postgres-deployment.yaml
    kubectl apply -f k8s/postgres-service.yaml
    ```
2.  **Deploy Valkey (Cache)**:
    ```bash
    kubectl apply -f k8s/valkey-deployment.yaml
    kubectl apply -f k8s/valkey-service.yaml
    ```
3.  **Deploy Application**:
    ```bash
    kubectl apply -f k8s/app-deployment.yaml
    kubectl apply -f k8s/app-service.yaml
    ```

#### Key K8s Features:
*   **Decoupled Services**: Database, Cache, and App are separated into individual deployments and services.
*   **Environment Injection**: Application configurations (DB URLs, Redis Hosts) are injected via K8s environment variables.
*   **Service Discovery**: The application connects to `postgres` and `valkey` using Kubernetes internal DNS service names.

### API Endpoint
*   **URL**: `POST http://localhost:8080/api/v1/stock`
*   **Body**:
    ```json
    {
      "symbol": "AAPL"
    }
    ```

---

## Testing

The implementation includes an **`ExceptionHandlingIntegrationTest`** suite that verifies the standardized error responses:
*   `mvn test -Dtest=ExceptionHandlingIntegrationTest`

---

## Project Structure

```text
src/main/java/com/hdfc/nasdaq_assignment/
├── bootstrap/          # Data loading logic
├── config/             # Redis and Caching config
├── controller/         # REST API Endpoints
├── dto/                # Request/Response objects
├── exception/          # Standardized Error Handling
├── filter/             # Performance tracking & TraceId
├── model/              # JPA Entities
├── repository/         # Data Access Layer
└── service/            # Business Logic & Caching
```

# Phase 2 – Scaffold payment-service

---

## 🧠 System Prompt

You are scaffolding a Spring Boot microservice named `payment-service`.  
Use Java 17 and Maven. Follow clean architecture principles.

---

## 💬 User Prompt

Create a Spring Boot microservice named `payment-service`.  
Requirements:
- Package: `com.example.paymentservice`
- REST controller for processing payments and refunds
- DTOs for request/response
- Service layer with interface and implementation
- Repository layer using Spring Data JPA
- Entity `Payment`: id, orderId, amount, method, status, timestamp
- Use H2 for dev, PostgreSQL for prod
- Include `/actuator/health` endpoint
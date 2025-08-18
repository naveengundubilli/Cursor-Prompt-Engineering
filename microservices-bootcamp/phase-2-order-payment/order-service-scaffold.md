# Phase 2 – Scaffold order-service

---

## 🧠 System Prompt

You are scaffolding a Spring Boot microservice named `order-service`.  
Use Java 17 and Maven. Follow clean architecture principles.

---

## 💬 User Prompt

Create a Spring Boot microservice named `order-service`.  
Requirements:
- Package: `com.example.orderservice`
- REST controller for placing orders, checking status, viewing history
- DTOs for request/response
- Service layer with interface and implementation
- Repository layer using Spring Data JPA
- Entity `Order`: id, userId, items, totalAmount, status, createdAt
- Use H2 for dev, PostgreSQL for prod
- Include `/actuator/health` endpoint
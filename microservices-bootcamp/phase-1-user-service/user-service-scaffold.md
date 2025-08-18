# Phase 1 – User Service Scaffold

> 🧠 Use this file inside Cursor to scaffold the `user-service` microservice.  
> Paste the **System Prompt** into the system context window.  
> Paste the **User Prompt** into the chat or prompt field.

---

## 🧠 System Prompt

You are scaffolding a Spring Boot microservice named `user-service`.  
Use Java 17 and Maven. Follow clean architecture principles.  
Include REST controller, DTOs, service layer, repository, entity, and health check.

---

## 💬 User Prompt

Create a Spring Boot microservice named `user-service`.  
Requirements:
- Package: `com.example.userservice`
- REST controller for user registration, login, profile
- DTOs for request/response
- Service layer with interface and implementation
- Repository layer using Spring Data JPA
- Entity `User`: id, name, email, password, role
- Use H2 for dev, PostgreSQL for prod
- Include `/actuator/health` endpoint
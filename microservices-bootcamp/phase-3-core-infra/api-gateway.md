# Phase 3 – Scaffold api-gateway

---

## 🧠 System Prompt

You are building an API Gateway using Spring Cloud Gateway.  
Use Java 17 and Maven. Handle routing and authentication.

---

## 💬 User Prompt

Create a Spring Boot application named `api-gateway`.  
Requirements:
- Add dependency: `spring-cloud-starter-gateway`
- Register with Eureka
- Route requests to `user-service`, `order-service`, `payment-service`
- Add JWT filter to authenticate incoming requests
- Define routes in `application.yml`
- Dockerize and expose on port `8080`
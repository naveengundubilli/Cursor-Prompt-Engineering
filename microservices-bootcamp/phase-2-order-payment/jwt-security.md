# Phase 2 – JWT Security for order-service and payment-service

---

## 🧠 System Prompt

You are securing `order-service` and `payment-service` using Spring Security and JWT.  
Use role-based access and secure token validation.

---

## 💬 User Prompt

Add JWT-based authentication to `order-service` and `payment-service`.  
Requirements:
- Secure all endpoints except `/actuator/health`
- Use `UserDetailsService` to load users
- Validate JWT on each request
- Use `ROLE_USER` for order access, `ROLE_ADMIN` for refund operations
- Include test cases for secured endpoints
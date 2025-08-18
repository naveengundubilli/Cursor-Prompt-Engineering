# Phase 1 – JWT-Based Security for user-service

---

## 🧠 System Prompt

You are implementing JWT-based authentication for a Spring Boot microservice named `user-service`.  
Use Spring Security and follow best practices for secure token handling and role-based access.

---

## 💬 User Prompt

Add JWT-based authentication to `user-service`.  
Requirements:
- Endpoint `/auth/login` returns JWT on valid credentials
- Secure all other endpoints with JWT
- Use `UserDetailsService` to load users
- Store passwords securely using BCrypt
- Add role-based access control (`ROLE_USER`, `ROLE_ADMIN`)
- Include filter to validate JWT on each request
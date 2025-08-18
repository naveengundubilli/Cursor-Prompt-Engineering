# Phase 4 – Test Eureka Service Discovery

---

## 🧠 System Prompt

You are testing dynamic service registration and discovery via Eureka.

---

## 💬 User Prompt

Write tests to verify that services register with `eureka-server`:  
Requirements:
- Use actuator endpoints to confirm registration
- Simulate service downtime and verify registry updates
- Test logical service name resolution via `RestTemplate` or `WebClient`
- Include retry logic for service unavailability
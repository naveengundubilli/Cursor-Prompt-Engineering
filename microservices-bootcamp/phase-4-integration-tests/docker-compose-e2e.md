# Phase 4 – End-to-End Test in Docker Compose

---

## 🧠 System Prompt

You are testing the full microservices stack in Docker Compose.

---

## 💬 User Prompt

Write a script or test suite to validate all services running in Docker Compose:  
Requirements:
- Wait for health checks to pass
- Authenticate and call endpoints via `api-gateway`
- Place order, trigger payment, fetch user profile
- Assert logs, metrics, and DB state
- Include cleanup and teardown logic
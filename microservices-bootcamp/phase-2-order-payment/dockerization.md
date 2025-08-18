# Phase 2 – Dockerize order-service and payment-service

---

## 🧠 System Prompt

You are containerizing `order-service` and `payment-service` for local and production use.

---

## 💬 User Prompt

Create Dockerfiles for both services:  
- Use OpenJDK 17 base image  
- Build with Maven  
- Expose ports 8082 and 8083  
- Include health checks in Docker Compose  
- Add `.dockerignore` for target/ and local configs
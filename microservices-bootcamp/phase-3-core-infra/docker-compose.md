# Phase 3 – Docker Compose Integration

---

## 🧠 System Prompt

You are creating a Docker Compose setup to run all microservices and infrastructure locally.

---

## 💬 User Prompt

Create a `docker-compose.yml` to run:
- `eureka-server` on port 8761
- `config-server` on port 8888
- `api-gateway` on port 8080
- `user-service`, `order-service`, `payment-service` on ports 8081–8083

Include:
- Health checks
- Service dependencies
- Mounted config repo
- `.env` file for secrets and DB credentials
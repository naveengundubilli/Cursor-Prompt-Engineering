# Phase 5 – Add Health Checks and Readiness Probes

---

## 🧠 System Prompt

You are preparing services for container orchestration.

---

## 💬 User Prompt

Add health and readiness probes to all services:  
Requirements:
- Use `/actuator/health` and `/actuator/info`
- Add custom health indicators for DB, config server, Eureka
- Configure Docker Compose and Kubernetes probes
- Fail fast on startup errors
- Include test cases for probe endpoints
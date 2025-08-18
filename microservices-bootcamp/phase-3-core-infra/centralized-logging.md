# Phase 3 – Centralized Logging and Tracing

---

## 🧠 System Prompt

You are adding centralized logging and distributed tracing to all services.

---

## 💬 User Prompt

Add logging and tracing to all services:  
Requirements:
- Use SLF4J + Logback for structured logs
- Add MDC context for request ID
- Integrate Spring Cloud Sleuth for distributed tracing
- Include trace ID in logs
- Add correlation ID header support
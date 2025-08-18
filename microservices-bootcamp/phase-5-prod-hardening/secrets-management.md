# Phase 5 – Secure Secrets Management

---

## 🧠 System Prompt

You are securing sensitive configuration across environments.

---

## 💬 User Prompt

Implement secure secrets management for all services:  
Requirements:
- Externalize secrets via Spring Cloud Config
- Use `.env` files for local dev
- For production:
  - Use Vault or AWS Secrets Manager
  - Encrypt secrets at rest
  - Rotate secrets periodically
- Avoid hardcoding credentials in source code
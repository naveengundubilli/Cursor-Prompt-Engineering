# Phase 5 – Enable TLS and Secure Communication

---

## 🧠 System Prompt

You are securing service-to-service and client communication.

---

## 💬 User Prompt

Enable HTTPS and secure internal communication:  
Requirements:
- Use TLS certificates for `api-gateway`
- Secure internal calls with mutual TLS or JWT
- Redirect HTTP to HTTPS
- Store certs securely (e.g., mounted secrets)
- Include test cases for secure endpoints
# Phase 4 – Test Order → Payment Flow

---

## 🧠 System Prompt

You are testing the end-to-end flow from order placement to payment processing.  
Use realistic data and validate service interaction.

---

## 💬 User Prompt

Write an integration test that simulates placing an order and triggering a payment.  
Requirements:
- Use `TestRestTemplate` or `WebTestClient`
- Authenticate with JWT before calling endpoints
- Place an order via `order-service`
- Trigger payment via `payment-service` using returned order ID
- Assert status codes, response bodies, and DB state
- Use Testcontainers for PostgreSQL
- Include trace ID propagation in logs
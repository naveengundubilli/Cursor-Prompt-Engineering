# Phase 4 – Test Distributed Tracing and Logging

---

## 🧠 System Prompt

You are testing observability across services using Sleuth or OpenTelemetry.

---

## 💬 User Prompt

Write tests to verify trace ID propagation across `user-service`, `order-service`, and `payment-service`:  
Requirements:
- Use Sleuth or OpenTelemetry
- Assert that trace ID is present in logs for each service
- Simulate a full request flow and extract trace context
- Include correlation ID header in requests
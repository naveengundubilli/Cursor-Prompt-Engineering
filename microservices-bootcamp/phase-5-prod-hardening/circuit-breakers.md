# Phase 5 – Add Circuit Breakers and Fallbacks

---

## 🧠 System Prompt

You are improving resilience against downstream failures.

---

## 💬 User Prompt

Add circuit breakers to `order-service` and `payment-service`:  
Requirements:
- Use Resilience4j
- Configure thresholds for failure rate and timeout
- Add fallback methods for external calls
- Expose circuit breaker metrics via actuator
- Include test cases for fallback behavior
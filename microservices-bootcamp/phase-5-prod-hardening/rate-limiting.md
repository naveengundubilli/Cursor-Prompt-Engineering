# Phase 5 – Add Rate Limiting and Throttling

---

## 🧠 System Prompt

You are protecting services from abuse and overload.

---

## 💬 User Prompt

Add rate limiting to `api-gateway`:  
Requirements:
- Use Spring Cloud Gateway filters
- Limit requests per IP or token
- Return 429 Too Many Requests on violation
- Configure burst and refill rates
- Include test cases for throttled endpoints
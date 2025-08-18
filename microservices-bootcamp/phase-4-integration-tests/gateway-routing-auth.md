# Phase 4 – Test API Gateway Routing and Auth

---

## 🧠 System Prompt

You are testing API Gateway routing and authentication.  
Ensure correct forwarding and access control.

---

## 💬 User Prompt

Write integration tests for `api-gateway`:  
Requirements:
- Test routing to `/user`, `/order`, `/payment` endpoints
- Include JWT in headers and validate access control
- Test rejection of invalid or expired tokens
- Assert correct forwarding and response codes
- Use mock services or local Docker setup
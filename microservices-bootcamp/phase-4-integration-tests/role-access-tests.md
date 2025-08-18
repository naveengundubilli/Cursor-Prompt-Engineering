# Phase 4 – Test Role-Based Access Across Services

---

## 🧠 System Prompt

You are testing role-based authorization across microservices.

---

## 💬 User Prompt

Write tests to verify access control for `ROLE_USER` and `ROLE_ADMIN`:  
Requirements:
- Authenticate with JWT for both roles
- Test access to `/order/history`, `/payment/refund`, `/admin/**`
- Assert 403 for unauthorized access
- Include audit logging for access attempts
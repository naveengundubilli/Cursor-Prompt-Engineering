# Phase 6 – Rollback Strategy

---

## 🧠 System Prompt

You are preparing for safe rollback in case of failure.

---

## 💬 User Prompt

Implement rollback strategy for production:  
Requirements:
- Keep previous stable image tag
- Use GitHub Actions or ArgoCD to revert
- Rollback config changes via Git
- Notify team and log rollback reason
- Include test cases for rollback flow
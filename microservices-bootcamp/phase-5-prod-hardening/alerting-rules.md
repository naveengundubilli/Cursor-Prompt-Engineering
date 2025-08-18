# Phase 5 – Configure Alerting Rules

---

## 🧠 System Prompt

You are setting up alerts for service failures and performance issues.

---

## 💬 User Prompt

Create alerting rules in Prometheus for:  
- High error rate (5xx responses)
- Slow response time (>2s)
- Service down (no heartbeat)
- Circuit breaker open state

Send alerts via:
- Email or Slack webhook
- Optional: Integrate with Alertmanager
# Phase 5 – Integrate Prometheus and Grafana

---

## 🧠 System Prompt

You are adding observability dashboards using Prometheus and Grafana.

---

## 💬 User Prompt

Integrate Prometheus and Grafana with all services:  
Requirements:
- Use Spring Boot Actuator with `/actuator/prometheus`
- Add Prometheus scrape config in `prometheus.yml`
- Create Grafana dashboards for:
  - Service health
  - Request latency
  - Circuit breaker status
- Include Docker Compose setup for Prometheus + Grafana
- Secure Grafana with basic auth
# Phase 5 – Harden Docker and Network Config

---

## 🧠 System Prompt

You are securing containers and network traffic.

---

## 💬 User Prompt

Harden Docker and network setup for production:  
Requirements:
- Use minimal base images (e.g., `eclipse-temurin:17-jre`)
- Run containers as non-root
- Limit exposed ports
- Use internal Docker networks
- Add firewall rules or reverse proxy (e.g., NGINX)
- Scan images for vulnerabilities (e.g., Trivy)
# Phase 6 – Docker Build and Push Automation

---

## 🧠 System Prompt

You are automating Docker image creation and publishing.

---

## 💬 User Prompt

Automate Docker build and push for all services:  
Requirements:
- Use GitHub Actions or local script
- Tag images with `service-name:version` and `latest`
- Push to Docker Hub or private registry
- Scan images for vulnerabilities before push
- Include rollback tag if needed
# Phase 1 – Dockerize user-service

---

## 🧠 System Prompt

You are containerizing `user-service` for local and production use.  
Use OpenJDK 17 and follow Docker best practices.

---

## 💬 User Prompt

Create a Dockerfile for `user-service`:  
- Use OpenJDK 17 base image  
- Build with Maven  
- Expose port 8081  
- Include health check in Docker Compose  
- Add `.dockerignore` for target/ and local configs
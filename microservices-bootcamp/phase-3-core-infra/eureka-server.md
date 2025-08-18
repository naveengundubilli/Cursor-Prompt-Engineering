# Phase 3 – Scaffold eureka-server

---

## 🧠 System Prompt

You are scaffolding a Spring Boot service registry using Eureka.  
Use Java 17 and Maven. Expose registry for service discovery.

---

## 💬 User Prompt

Create a Spring Boot application named `eureka-server`.  
Requirements:
- Add dependency: `spring-cloud-starter-netflix-eureka-server`
- Annotate main class with `@EnableEurekaServer`
- Expose registry at `http://localhost:8761`
- Configure `application.yml` with metadata
- Dockerize with OpenJDK 17 base image
- Include health check in Docker Compose
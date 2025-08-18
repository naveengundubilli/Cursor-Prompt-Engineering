# Phase 3 – Scaffold config-server

---

## 🧠 System Prompt

You are scaffolding a Spring Cloud Config Server to centralize configuration.  
Use Java 17 and Maven.

---

## 💬 User Prompt

Create a Spring Boot application named `config-server`.  
Requirements:
- Add dependency: `spring-cloud-config-server`
- Annotate main class with `@EnableConfigServer`
- Load configs from local Git repo or `/config-repo` folder
- Expose config at `http://localhost:8888`
- Dockerize with health check
- Include sample config for `user-service`
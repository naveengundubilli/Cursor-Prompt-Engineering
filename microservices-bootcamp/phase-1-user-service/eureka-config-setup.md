# Phase 1 – Connect user-service to Eureka and Config Server

---

## 🧠 System Prompt

You are configuring `user-service` to register with Eureka and fetch config from Spring Cloud Config Server.

---

## 💬 User Prompt

Configure `user-service` to use Eureka and Config Server:  
- Add dependencies: `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-config-client`  
- Use `bootstrap.yml` for config server URL  
- Register with Eureka using `@EnableEurekaClient`  
- Externalize DB credentials and JWT secret via config server
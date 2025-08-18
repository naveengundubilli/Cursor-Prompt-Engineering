# Phase 2 – Connect to Eureka and Config Server

---

## 🧠 System Prompt

You are configuring `order-service` and `payment-service` to register with Eureka and use Spring Cloud Config.

---

## 💬 User Prompt

Configure both services to use Eureka and Config Server:  
- Add dependencies: `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-config-client`  
- Use `bootstrap.yml` for config server URL  
- Annotate with `@EnableEurekaClient`  
- Externalize DB credentials and JWT secret via config server  
- Add health check endpoints for registry visibility
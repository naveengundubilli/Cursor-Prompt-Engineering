# Phase 3 – Service Registration with Eureka and Config Server

---

## 🧠 System Prompt

You are configuring microservices to register with Eureka and fetch config from Spring Cloud Config Server.

---

## 💬 User Prompt

Configure `user-service`, `order-service`, and `payment-service` to use Eureka and Config Server.  
Requirements:
- Add dependencies: `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-config-client`
- Use `bootstrap.yml` for config server URL
- Annotate with `@EnableEurekaClient`
- Externalize DB credentials and JWT secret via config server
- Add health check endpoints
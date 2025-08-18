# Phase 4 – Test Config Server Bootstrapping

---

## 🧠 System Prompt

You are testing centralized configuration loading via Spring Cloud Config Server.

---

## 💬 User Prompt

Write tests to verify that services load config from `config-server`:  
Requirements:
- Use `@SpringBootTest` with `ConfigDataLocation`
- Assert that DB credentials and JWT secrets are loaded correctly
- Simulate config changes and refresh scope
- Include fallback behavior if config server is unavailable
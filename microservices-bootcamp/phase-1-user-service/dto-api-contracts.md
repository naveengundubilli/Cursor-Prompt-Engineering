# Phase 1 – DTOs and API Contracts for user-service

---

## 🧠 System Prompt

You are defining clean DTOs and API contracts for `user-service`.  
Use validation annotations and separate DTOs from entities.

---

## 💬 User Prompt

Create DTOs for `user-service`:  
- `UserRegistrationRequest`: name, email, password  
- `UserLoginRequest`: email, password  
- `UserProfileResponse`: id, name, email, role  

Ensure:
- Validation annotations (`@NotBlank`, `@Email`, etc.)
- Separation between entity and DTO
- Use `ModelMapper` or manual mapping in service layer
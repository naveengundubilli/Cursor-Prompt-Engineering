# Phase 2 – DTOs and API Contracts

---

## 🧠 System Prompt

You are defining DTOs and API contracts for `order-service` and `payment-service`.  
Use validation annotations and separate DTOs from entities.

---

## 💬 User Prompt

Create DTOs for both services:  
- `OrderRequest`: userId, items, totalAmount  
- `OrderResponse`: id, status, createdAt  
- `PaymentRequest`: orderId, amount, method  
- `PaymentResponse`: id, status, timestamp  

Ensure:
- Validation annotations (`@NotBlank`, `@Min`, etc.)
- Separation between entity and DTO
- Use ModelMapper or manual mapping
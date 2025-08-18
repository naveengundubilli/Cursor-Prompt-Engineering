# Phase 6 – GitHub Actions CI/CD Setup

---

## 🧠 System Prompt

You are automating builds, tests, and deployments using GitHub Actions.

---

## 💬 User Prompt

Create GitHub Actions workflows for all services:  
Requirements:
- Trigger on push to `main` and PRs
- Steps:
  - Checkout code
  - Build with Maven
  - Run unit tests
  - Build and push Docker image
  - Deploy to staging (optional)
- Use reusable workflows for common steps
- Store secrets in GitHub Actions vault
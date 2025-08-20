# KisanConnect (Windows + No-Docker Foundation)

## Quick Start (Windows)
1) Run PowerShell as admin (or with exec policy for local scripts):
   `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`
2) Bootstrap Gradle wrapper and basic modules:
   `./scripts/windows/bootstrap_m1.ps1`
3) Start Java services in **No-Docker** mode:
   `./gradlew :services:identity-svc:bootRun --args="--spring.profiles.active=nodocker"`
   `./gradlew :services:profile-svc:bootRun --args="--spring.profiles.active=nodocker"`
4) Start ML FastAPI:
   `./scripts/windows/run_ml.ps1`
5) Health checks:
   `./scripts/windows/check_m1.ps1`

> Later, when Docker Desktop is installed, you can run `docker compose up -d` and switch off nodocker profiles.


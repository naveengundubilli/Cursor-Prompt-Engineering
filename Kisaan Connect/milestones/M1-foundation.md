# M1-foundation — Foundation: Monorepo, shared libs, local stack

## Goals
- _Copy from cursor_prompts_full.json_

## Checklist
- [ ] Scaffold code per prompts for M1-foundation
- [ ] Commit and push branch `feature/m1-foundation`
- [ ] `docker compose up -d` (infra healthy)
- [ ] Health endpoints: `curl http://localhost:<svc>/actuator/health` → UP
- [ ] OpenAPI docs reachable at `/swagger-ui` or `/docs`

## Commands (local dev)
```bash
# Start infra
docker compose up -d

# Tail logs (example)
docker compose logs -f postgres redis kafka zookeeper minio

# Build all Java services
./gradlew clean build -x test

# Run tests
./gradlew test
```

## Smoke Tests
- _Use the milestone-specific smoke tests from cursor_prompts_full.json → post_setup_checks_
- Record results in `SMOKE-M1-foundation.md`

## Demo Script
1) Open terminal with services running.
2) Run the cURL sequence from this file's **Smoke Tests**.
3) Show logs of affected services.
4) Capture screenshots, commit to `docs/demos/M1-foundation/`.

## Exit Criteria
- [ ] All smoke tests pass
- [ ] README updated with any deviations
- [ ] Tag release `milestone/M1-foundation`

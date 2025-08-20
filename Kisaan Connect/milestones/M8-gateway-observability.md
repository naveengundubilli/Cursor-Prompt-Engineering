# M8-gateway-observability — API Gateway + SLO instrumentation

## Goals
- _Copy from cursor_prompts_full.json_

## Checklist
- [ ] Scaffold code per prompts for M8-gateway-observability
- [ ] Commit and push branch `feature/m8-gateway-observability`
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
- Record results in `SMOKE-M8-gateway-observability.md`

## Demo Script
1) Open terminal with services running.
2) Run the cURL sequence from this file's **Smoke Tests**.
3) Show logs of affected services.
4) Capture screenshots, commit to `docs/demos/M8-gateway-observability/`.

## Exit Criteria
- [ ] All smoke tests pass
- [ ] README updated with any deviations
- [ ] Tag release `milestone/M8-gateway-observability`

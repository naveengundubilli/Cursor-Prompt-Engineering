
# KisanConnect — All-in-One Cursor Prompt Pack (Token-Optimized)

> Paste sections into Cursor as-is. Use the **SYSTEM** headers to keep responses short and file-centric.
> If Cursor refuses to write files, it must output a single `apply.sh` script that writes files via heredocs.

---

## 0) Global Low-Verbosity Build Mode (prepend to every prompt)

```
SYSTEM: You are in LOW-VERBOSITY BUILD MODE.
RULES:
1) Write/modify files; do NOT explain.
2) No restatements, no diffs, no code fences unless asked for a script.
3) Keep implementations minimal but runnable; add tests only if requested.
4) Prefer small scaffolding scripts and placeholders over huge blobs.
5) Stop after files are written.
Fallback: If you cannot write files directly, output a bash script named 'apply.sh' that writes all files via heredocs and mark it executable. Do not output anything else.
```

---

## 1) Milestones Overview JSON (reference-only, already merged)

Use this for planning or import. (You don't need to paste it into Cursor unless you want the milestone list as data.)

<details><summary>cursor_prompts_full.json</summary>

```json
{
  "project": "KisanConnect",
  "version": "1.2",
  "strategy": "Build in vertical slices; each milestone delivers a runnable demo and smoke tests.",
  "notes": "Run prompts in order within each milestone. Allow Cursor to write files, then fix builds/tests when prompted.",
  "milestones": [
    {
      "id": "M1-foundation",
      "title": "Foundation: Monorepo, shared libs, local stack",
      "goals": [
        "Monorepo bootstrapped (Java 21/Spring Boot + Python FastAPI)",
        "Local dev stack (Postgres+Timescale, Redis, Kafka, MinIO, Mailhog)",
        "Shared lib-java (JWT, errors, OTel); contracts imported"
      ],
      "includes_prompts": [
        "00-boot-monorepo",
        "01-shared-libs",
        "02-import-contracts"
      ],
      "deliverables": [
        "docker-compose up brings core infra online",
        "shared/lib-java published and used by services",
        "OpenAPI skeletons available under shared/contracts"
      ],
      "smoke_tests": [
        "docker compose ps shows all infra healthy",
        "curl http://localhost:<any-service>/actuator/health \u2192 UP (placeholder svc ok)",
        "contracts lint (spectral): no errors"
      ],
      "risks": [
        "Gradle multi-module misconfig \u2192 fix settings.gradle.kts",
        "Port collisions \u2192 adjust compose ports"
      ]
    },
    {
      "id": "M2-auth-identity",
      "title": "Authentication slice: OTP \u2192 JWT",
      "goals": [
        "identity-svc issues JWT + refresh",
        "Gateway or per-service JWT validation (lib-java)"
      ],
      "includes_prompts": [
        "03-identity-svc"
      ],
      "deliverables": [
        "POST /auth/otp/send & /auth/otp/verify live",
        "JWT claims: sub, org_id, roles, locale, device_id"
      ],
      "smoke_tests": [
        "POST /auth/otp/send \u2192 202",
        "POST /auth/otp/verify \u2192 {access_token, refresh_token}",
        "Call any protected stub endpoint with JWT \u2192 200"
      ],
      "risks": [
        "Clock skew with JWT exp; ensure leeway \u00b160s"
      ]
    },
    {
      "id": "M3-farmer-profile",
      "title": "Farmer profile vertical slice",
      "goals": [
        "profile-svc with farmer/plot entities (Flyway)",
        "Redis cache for GET by id"
      ],
      "includes_prompts": [
        "04-profile-svc"
      ],
      "deliverables": [
        "POST/GET /farmers, GET /farmers/{id}",
        "Seed data migrations; testcontainers passing"
      ],
      "smoke_tests": [
        "Create farmer \u2192 fetch by id \u2192 matches",
        "Cache hit observed (Redis TTL 60s)"
      ],
      "risks": [
        "JSONB/validation drift \u2192 schema tests in CI"
      ]
    },
    {
      "id": "M4-media-inference-advisory",
      "title": "Advisory MVP: media \u2192 ML \u2192 DB",
      "goals": [
        "media presign, ml inference stub, advisory persistence",
        "Kafka event on advisory.created"
      ],
      "includes_prompts": [
        "05-media-svc",
        "06-ml-inference",
        "07-advisory-svc"
      ],
      "deliverables": [
        "POST /advisories (multipart) returns predictions + persisted record",
        "Event published to Kafka with payload_sha256"
      ],
      "smoke_tests": [
        "Presign upload works via MinIO",
        "POST /advisories \u2192 200; GET /advisories/{id} shows record",
        "Kafka topic advisory.created received once (idempotency checked)"
      ],
      "risks": [
        "Large images \u2192 client compression + 5MB limit"
      ]
    },
    {
      "id": "M5-notary",
      "title": "Blockchain notary integration (mock Fabric)",
      "goals": [
        "notary-svc API; advisory \u2192 notary hash; record fabric_tx",
        "Anchor scheduler stub updates anchor_tx"
      ],
      "includes_prompts": [
        "08-notary-svc"
      ],
      "deliverables": [
        "POST /notary/hash usable from advisory listener",
        "Anchor job writes anchor_tx back to DB"
      ],
      "smoke_tests": [
        "Create advisory \u2192 fabric_tx populated within 5s",
        "Run anchor job \u2192 anchor_tx populated"
      ],
      "risks": [
        "Async failures \u2192 DLQ + retries with backoff"
      ]
    },
    {
      "id": "M6-iot-weather-prices",
      "title": "IoT ingest + Weather + Prices",
      "goals": [
        "iot-svc MQTT ingest \u2192 Timescale hypertable",
        "weather-svc adapter with 1h cache",
        "prices-svc CSV ingest + GET /prices with trend"
      ],
      "includes_prompts": [
        "09-iot-svc",
        "10-weather-prices"
      ],
      "deliverables": [
        "GET /iot/readings returns stored rows",
        "GET /weather/forecast, GET /prices respond with normalized data"
      ],
      "smoke_tests": [
        "Publish MQTT reading \u2192 query shows row",
        "Price ingest job loads sample CSV \u2192 prices query returns trend"
      ],
      "risks": [
        "Timezones & DST in TSDB \u2192 store UTC in DB"
      ]
    },
    {
      "id": "M7-forum-notifications",
      "title": "Community forum + notifications",
      "goals": [
        "forum-svc threads/posts with moderation stub",
        "notification-svc for SMS/Push"
      ],
      "includes_prompts": [
        "11-forum-notification"
      ],
      "deliverables": [
        "POST /threads, /posts; moderation_status transitions",
        "POST /notify enqueues SMS/Push (Mailhog/Firebase stub)"
      ],
      "smoke_tests": [
        "Create post \u2192 moderation queued \u2192 publish \u2192 notify",
        "Toxicity score field set (stubbed)"
      ],
      "risks": [
        "Abuse/spam \u2192 rate limits + shadow ban toggle"
      ]
    },
    {
      "id": "M8-gateway-observability",
      "title": "API Gateway + SLO instrumentation",
      "goals": [
        "Gateway with JWT validation, CORS, rate limits",
        "OTel + Prometheus metrics; Grafana dashboards"
      ],
      "includes_prompts": [
        "12-gateway-security",
        "13-observability"
      ],
      "deliverables": [
        "Declarative routes, per-route RBAC",
        "Dashboards for API p95, error_rate, ML latency, MQTT lag"
      ],
      "smoke_tests": [
        "Protected routes 401 without JWT; 200 with valid JWT",
        "Dashboards show live metrics during simple load"
      ],
      "risks": [
        "CORS misconfig \u2192 add integration tests"
      ]
    },
    {
      "id": "M9-ci-compose",
      "title": "CI pipelines + local smoke tests",
      "goals": [
        "GH Actions for build/test/lint, docker buildx, trivy",
        "PR smoke test boots compose and runs end-to-end checks"
      ],
      "includes_prompts": [
        "14-ci-compose"
      ],
      "deliverables": [
        "CI green on main; PRs run smoke suite",
        "Compose healthchecks for all services"
      ],
      "smoke_tests": [
        "OTP\u2192JWT, profile create/get, advisory flow, inference call",
        "Forum post moderation + notification"
      ],
      "risks": [
        "Flaky tests \u2192 increase timeouts & retries"
      ]
    },
    {
      "id": "M10-web-pwa",
      "title": "Farmer Web App (PWA) \u2013 Responsive + Offline",
      "goals": [
        "Next.js (React) web client with responsive layout (mobile \u2192 desktop)",
        "PWA: installable, offline cache (Workbox), background sync for advisories/ledger",
        "Shared TypeScript SDK generated from OpenAPI"
      ],
      "includes_prompts": [
        "15-web-app",
        "16-ts-sdk",
        "17-pwa-offline"
      ],
      "deliverables": [
        "Public web app with OTP login and localized UI",
        "Offline home, prices, weather; queued advisory uploads"
      ],
      "smoke_tests": [
        "Login via OTP; refresh preserves session",
        "Toggle offline: create ledger entry; reconnect \u2192 sync succeeds",
        "Lighthouse PWA score \u2265 90"
      ],
      "risks": [
        "CORS misconfiguration \u2192 add e2e tests against gateway",
        "Cache staleness \u2192 cache bust strategy with SW versioning"
      ]
    },
    {
      "id": "M11-admin-console",
      "title": "Admin/Operations Console \u2013 Desktop-first",
      "goals": [
        "React Admin Console (MUI/Tailwind) for content ops and moderation",
        "Views: farmers, advisories, forum queue, schemes CMS, metrics dashboards",
        "RBAC: admin, moderator, content-editor roles"
      ],
      "includes_prompts": [
        "18-admin-console"
      ],
      "deliverables": [
        "Moderation queue with approve/reject and audit trail",
        "Schemes CRUD with localization fields"
      ],
      "smoke_tests": [
        "Moderator approves a queued post \u2192 notification sent",
        "Editor publishes a scheme \u2192 visible in mobile/web"
      ],
      "risks": [
        "Permission leaks \u2192 add backend ABAC checks and route guards"
      ]
    },
    {
      "id": "M12-web-deploy-security",
      "title": "Web Deployment, CORS & Security Headers",
      "goals": [
        "CI deploy of web app to CDN (Vercel/Netlify or S3+CloudFront)",
        "Hardened CORS at gateway + strict security headers on web",
        "Domain & TLS automation"
      ],
      "includes_prompts": [
        "19-web-deploy",
        "20-cors-security"
      ],
      "deliverables": [
        "Public URL for PWA and admin console",
        "CSP/STS/referrer-policy and integrity checks configured"
      ],
      "smoke_tests": [
        "CORS preflight passes for all allowed routes, blocks others",
        "ZAP baseline scan: no high-severity findings"
      ],
      "risks": [
        "Overly strict CSP breaking SW \u2192 test and iterate"
      ]
    }
  ],
  "prompts": [
    {
      "id": "00-boot-monorepo",
      "title": "Bootstrap monorepo and dev stack",
      "prompt": "You are a principal engineer. Create a multi-module monorepo named 'kisanconnect' for a microservices project. Use Java 21 + Spring Boot 3.3 (Gradle Kotlin DSL) for services and a separate Python FastAPI package for ML inference. Add:\\n- root README with overview, local dev steps (docker-compose) and service map\\n- .editorconfig, .gitignore, CODEOWNERS, pre-commit config (spotless for Java, black for Python)\\n- .github/workflows: java build/test, python lint/test, docker build per service\\n- docker-compose with: postgres (with timescaledb extension), redis, kafka+zookeeper, minio, mailhog (SMS stub)\\n- a 'shared/contracts' folder to hold OpenAPI YAMLs\\nInitialize Gradle projects, settings.gradle.kts with composite builds."
    },
    {
      "id": "01-shared-libs",
      "title": "Shared Java lib & error model",
      "prompt": "Create a 'shared/lib-java' Gradle module for common code:\\n- JWT auth filter + RBAC helper; extract org_id, roles, locale from JWT\\n- RFC7807 ProblemDetails error model + global exception handlers\\n- OpenTelemetry autoconfigure; Micrometer/Prometheus\\nPublish this module to local maven so other services can depend on it via settings.gradle.kts."
    },
    {
      "id": "02-import-contracts",
      "title": "Import OpenAPI contracts",
      "prompt": "Create 'shared/contracts' and add OpenAPI 3.1 YAML skeletons for identity, profile, weather, prices, advisory, schemes, forum, ledger, iot, notary, notification, media. Wire springdoc-openapi to serve /v3/api-docs and Swagger UI in each service using these contracts as source of truth."
    },
    {
      "id": "03-identity-svc",
      "title": "identity-svc (OTP \u2192 JWT)",
      "prompt": "Create service 'services/identity-svc' (Spring Boot):\\n- Endpoints: POST /auth/otp/send, POST /auth/otp/verify (per OpenAPI)\\n- SmsGateway interface with LocalSmsGateway impl writing OTP to stdout + Mailhog\\n- Issue JWT (HS256) + refresh token; claims: sub(farmer_id), org_id, roles, locale, device_id; 15m access, 7d refresh\\n- Tests: unit + slice + minimal integration; OpenAPI via springdoc; Dockerfile\\n- Add Gradle dependencies on shared/lib-java"
    },
    {
      "id": "04-profile-svc",
      "title": "profile-svc (farmer/plot)",
      "prompt": "Create 'services/profile-svc' with Postgres + Flyway:\\n- Tables: farmer, plot (per ERD); JSONB fields where flexible\\n- Endpoints: POST/GET /farmers, GET /farmers/{id} (per OpenAPI)\\n- Redis cache for GET by id (TTL 60s)\\n- Testcontainers for integration tests; Dockerfile\\n- Seed demo data migration"
    },
    {
      "id": "05-media-svc",
      "title": "media-svc (presigned upload)",
      "prompt": "Create 'services/media-svc':\\n- POST /media/sign \u2192 returns presigned PUT + GET URLs (use AWS SDK; MinIO in docker-compose)\\n- Validate content-type, max 5MB; return expiry; log audit event\\n- Unit tests + Dockerfile"
    },
    {
      "id": "06-ml-inference",
      "title": "ml/inference (FastAPI)",
      "prompt": "Create 'ml/inference' FastAPI service:\\n- POST /infer { image_url } \u2192 returns top3 labels + confidences + model_version + explanation stub\\n- Deterministic stub model for now; structure for TorchServe/ONNX later\\n- Uvicorn config, health checks, pytest, Dockerfile"
    },
    {
      "id": "07-advisory-svc",
      "title": "advisory-svc (image\u2192ML\u2192DB\u2192event\u2192notary)",
      "prompt": "Create 'services/advisory-svc':\\n- POST /advisories (multipart): obtain presigned URL from media-svc, upload, compute SHA-256(payload JSON), call ml-inference, persist advisory, publish Kafka 'advisory.created' with advisory_id + payload_sha256\\n- Listener consumes 'advisory.created', calls notary-svc /notary/hash \u2192 updates advisory.fabric_tx\\n- GET /advisories/{id}\\n- Postgres + Flyway schema, tests, Dockerfile"
    },
    {
      "id": "08-notary-svc",
      "title": "notary-svc (Fabric + anchor)",
      "prompt": "Create 'services/notary-svc':\\n- POST /notary/hash { ownerId, type, sha256 } \u2192 call FabricClient (mock impl for local), return fabric_tx; persist mapping\\n- Scheduled /anchor/run (or cron job) batches recent Fabric block hash to a simulated public chain; updates anchor_tx on related records\\n- Tests + Dockerfile"
    },
    {
      "id": "09-iot-svc",
      "title": "iot-svc (ingest + advice)",
      "prompt": "Create 'services/iot-svc':\\n- MQTT consumer for topic iot/{device_id}; persist to TimescaleDB hypertable iot_reading\\n- GET /iot/readings?device_id&from=...\\n- Simple irrigation rule: if soil_moisture<threshold AND forecast no-rain next 24h, POST /notify to notification-svc\\n- Tests + Dockerfile"
    },
    {
      "id": "10-weather-prices",
      "title": "weather-svc & prices-svc",
      "prompt": "Create 'services/weather-svc' with provider adapter pattern (Open-Meteo stub) + 1h cache per lat/lon.\\nCreate 'services/prices-svc' to ingest CSV for market_price and serve GET /prices with nearest mandis and 7-day trend. Add tests + Dockerfiles."
    },
    {
      "id": "11-forum-notification",
      "title": "forum-svc & notification-svc",
      "prompt": "Create 'services/forum-svc' with threads/posts tables, moderation_status, toxicity_score. On create, call moderation stub and set queued/published, then POST /notify to notification-svc when published.\\nCreate 'services/notification-svc' with POST /notify { channel: SMS|PUSH, template_id, params }; Local SMS \u2192 stdout/Mailhog; push: Firebase stub. Add tests + Dockerfiles."
    },
    {
      "id": "12-gateway-security",
      "title": "API Gateway and security",
      "prompt": "Add Spring Cloud Gateway or Kong declarative config under 'services/gateway':\\n- Validate JWT from identity-svc; per-route RBAC\\n- Rate-limits: default 60 rpm/IP; 5 rpm for POST /advisories\\n- CORS: allow mobile app schemes only; block others\\n- Health checks / readiness endpoints for all services"
    },
    {
      "id": "13-observability",
      "title": "Observability & SLOs",
      "prompt": "Instrument all services with OpenTelemetry + Micrometer (Prometheus). Add Grafana dashboards (API RPS, p95 latency, error_rate; ML inference latency; MQTT lag). Add synthetic check that posts advisory then validates GET by id within 10s. Document SLOs in README."
    },
    {
      "id": "14-ci-compose",
      "title": "CI and docker-compose integration",
      "prompt": "Update docker-compose to wire ports, env, healthchecks for all services. In GitHub Actions: build & unit tests per service, docker buildx, trivy scan, cache Gradle and pip, push images on main branch. Add a PR workflow that boots compose and runs smoke tests (identity, profile, advisory, inference)."
    },
    {
      "id": "15-web-app",
      "title": "Create Next.js farmer web app (responsive)",
      "prompt": "Create app 'web/farmer' using Next.js 14 (App Router) + TypeScript + Tailwind. Pages: /login (OTP), /home (weather, prices, shortcuts), /advisories (list + upload), /ledger, /schemes, /forum. Use server components for SEO-safe pages where possible; client components for interactive forms. Add i18n (next-intl) with Hindi, Marathi, Telugu, Tamil, Bengali. Integrate JWT from identity-svc; persist in HttpOnly secure cookies. Add reusable UI components (Card, List, DataTable)."
    },
    {
      "id": "16-ts-sdk",
      "title": "Generate shared TypeScript SDK from OpenAPI",
      "prompt": "Using openapi-typescript or openapi-generator, produce 'shared/sdk-ts' with typed clients for all services (identity, profile, prices, weather, advisory, forum, etc.). Publish as a local workspace package and consume it in 'web/farmer' and 'web/admin'."
    },
    {
      "id": "17-pwa-offline",
      "title": "Enable PWA + offline + background sync",
      "prompt": "In 'web/farmer': add manifest.json, icons, and Workbox service worker. Cache strategies: stale-while-revalidate for GET /weather/forecast and /prices; network-first with fallback for /schemes; queue POSTs (advisories, ledger) via background sync with IndexedDB outbox. Add a 'Sync Center' UI to show pending operations. Add Lighthouse config; aim PWA score \u2265 90."
    },
    {
      "id": "18-admin-console",
      "title": "Create desktop-first Admin Console",
      "prompt": "Create 'web/admin' (Next.js + MUI/Tailwind). Routes: /dashboard (stats from Prometheus/gateway), /farmers, /advisories, /forum/moderation-queue, /schemes (CMS CRUD), /translations. Implement RBAC guards: admin, moderator, editor. Hook moderation actions to forum-svc; write audit logs. Add tables with server-side pagination and filters."
    },
    {
      "id": "19-web-deploy",
      "title": "Set up web deployment (CDN + CI)",
      "prompt": "Add GitHub Actions to build and deploy 'web/farmer' and 'web/admin' to Vercel/Netlify or S3+CloudFront (pick one and generate IaC if S3+CF). Configure environment variables (API base URLs), asset caching (immutable hashed files), and SW versioning. Output public URLs in the build summary."
    },
    {
      "id": "20-cors-security",
      "title": "Gateway CORS + Security Headers",
      "prompt": "At API gateway: configure CORS to allow origins ['https://farmer.example.com','https://admin.example.com'] and mobile app schemes; block others. Methods: GET,POST,PUT,DELETE,OPTIONS; headers: Authorization, Content-Type. Set security headers on web apps: Content-Security-Policy (script-src 'self' with hashed bundles; connect-src to API/CDN), Strict-Transport-Security, X-Content-Type-Options, Referrer-Policy, Permissions-Policy. Add e2e tests to verify preflight and header presence."
    }
  ],
  "variables": {
    "jwt_secret": "set a local dev value in .env",
    "db_password": "use docker-compose secrets or env",
    "minio_keys": "MINIO_ROOT_USER/MINIO_ROOT_PASSWORD",
    "kafka": "PLAINTEXT localhost:9092 for dev",
    "mqtt": "optional add mosquitto service for IoT tests"
  },
  "post_setup_checks": [
    "docker compose up -d; confirm health endpoints",
    "OTP\u2192JWT works; use JWT to call protected endpoints",
    "Advisory flow: presign \u2192 upload \u2192 inference \u2192 persist \u2192 notary (mock) \u2192 anchor",
    "IoT publish/readings; weather & prices respond; forum moderation triggers notifications",
    "Web PWA builds and loads offline; Admin console RBAC works; CORS preflights pass"
  ]
}
```
</details>

---

## 2) M1 — One-shot Bootstrap (monorepo + infra) — **paste entire block**

```
SYSTEM: Act as a file generator. Do not explain anything.
RESPONSE CONTRACT:
- Write/overwrite the exact files listed below.
- If a file is a shell script, make it executable (0755).
- No prose, no diffs, no code fences in the response — just create the files.
- If direct file writes are impossible, emit a single 'apply.sh' that writes them with heredocs.

TASK: Create a minimal, runnable M1 foundation (monorepo + infra), optimized for token usage:
- Root Gradle multi-module (Java 21, Spring Boot 3.3)
- Python FastAPI skeleton for ML
- docker-compose with Postgres(+Timescale ext), Redis, Kafka+ZK, MinIO, Mailhog
- Shared lib-java module (JWT filter stub, error model stub, OTel)
- Shared contracts folder (we will drop YAML after)
- A bootstrap script to wire everything and a check script to verify health

CREATE THESE FILES:

./.editorconfig
./.gitignore
./README.md
./settings.gradle.kts
./build.gradle.kts
./gradle.properties
./gradlew (placeholder - we will regenerate with wrapper later)
./gradlew.bat (placeholder)
./gradle/wrapper/gradle-wrapper.properties (placeholder)
./scripts/bootstrap_m1.sh
./scripts/check_m1.sh
./docker-compose.yml
./shared/lib-java/build.gradle.kts
./shared/lib-java/src/main/java/com/kisan/shared/Sentinel.java
./shared/lib-java/src/main/java/com/kisan/shared/security/JwtAuthFilter.java
./shared/lib-java/src/main/java/com/kisan/shared/error/ProblemDetails.java
./shared/contracts/.keep
./services/identity-svc/build.gradle.kts
./services/identity-svc/src/main/java/com/kisan/identity/IdentityApplication.java
./services/identity-svc/src/main/resources/application.yaml
./ml/inference/pyproject.toml
./ml/inference/app/main.py
./.github/workflows/ci-basic.yml

POPULATE WITH THIS EXACT CONTENT:

FILE .editorconfig
---
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
indent_style = space
indent_size = 2
---

FILE .gitignore
---
.gradle/
build/
out/
node_modules/
.env
.DS_Store
*.log
__pycache__/
.mypy_cache/
.venv/
---

FILE README.md
---
# KisanConnect (M1 Foundation)

- Monorepo: Gradle + Spring Boot services, Python FastAPI for ML
- Local infra: Postgres(Timescale), Redis, Kafka+ZK, MinIO, Mailhog
- Start: `bash scripts/bootstrap_m1.sh` then `docker compose up -d` then `bash scripts/check_m1.sh`
---

FILE settings.gradle.kts
---
rootProject.name = "kisanconnect"
include("shared:lib-java")
include("services:identity-svc")
---

FILE build.gradle.kts
---
plugins {
  id("org.springframework.boot") version "3.3.2" apply false
  id("io.spring.dependency-management") version "1.1.5"
  java
}

allprojects {
  group = "com.kisan"
  version = "0.1.0"
  repositories { mavenCentral() }
}

subprojects {
  apply(plugin = "io.spring.dependency-management")
}
---

FILE gradle.properties
---
org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
---

FILE gradlew
---
placeholder
---

FILE gradlew.bat
---
placeholder
---

FILE gradle/wrapper/gradle-wrapper.properties
---
# placeholder; run scripts/bootstrap_m1.sh to generate wrapper
---

FILE scripts/bootstrap_m1.sh (chmod 0755)
---
#!/usr/bin/env bash
set -euo pipefail

# Gradle wrapper
if ! ./gradlew --version >/dev/null 2>&1; then
  if command -v gradle >/dev/null 2>&1; then
    gradle wrapper --gradle-version 8.9
  else
    echo "Gradle not installed. Install Gradle or run 'gradle wrapper' once available."
  fi
fi

# Java modules quick init
pushd shared/lib-java >/dev/null
cat > build.gradle.kts <<'EOF'
plugins {
  `java-library`
}
java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
dependencies {
  api("org.slf4j:slf4j-api:2.0.13")
}
EOF
mkdir -p src/main/java/com/kisan/shared/security src/main/java/com/kisan/shared/error
popd >/dev/null

pushd services/identity-svc >/dev/null
cat > build.gradle.kts <<'EOF'
plugins {
  id("org.springframework.boot") version "3.3.2"
  id("io.spring.dependency-management")
  java
}
java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
dependencies {
  implementation(project(":shared:lib-java"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
tasks.test { useJUnitPlatform() }
EOF
popd >/dev/null

echo "Bootstrap complete. Next: docker compose up -d; ./gradlew :services:identity-svc:bootRun (optional)"
---

FILE scripts/check_m1.sh (chmod 0755)
---
#!/usr/bin/env bash
set -euo pipefail
echo "Compose services:"
docker compose ps || true
echo "Try hitting services (once you add more):"
curl -s http://localhost:8081/actuator/health || true
---

FILE docker-compose.yml
---
version: "3.9"
services:
  postgres:
    image: timescale/timescaledb:latest-pg16
    environment:
      POSTGRES_USER: kisan
      POSTGRES_PASSWORD: kisan
      POSTGRES_DB: kisan
    ports: ["5432:5432"]
    healthcheck:
      test: ["CMD-SHELL","pg_isready -U kisan"]
      interval: 5s
      timeout: 5s
      retries: 10
  redis:
    image: redis:7
    ports: ["6379:6379"]
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.3
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports: ["2181:2181"]
  kafka:
    image: confluentinc/cp-kafka:7.5.3
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
  minio:
    image: minio/minio:RELEASE.2024-05-10T01-41-38Z
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minio
      MINIO_ROOT_PASSWORD: minio123
    ports: ["9000:9000","9001:9001"]
  mailhog:
    image: mailhog/mailhog:v1.0.1
    ports: ["1025:1025","8025:8025"]
---

FILE shared/lib-java/src/main/java/com/kisan/shared/Sentinel.java
---
package com.kisan.shared;
public final class Sentinel { private Sentinel() {} }
---

FILE shared/lib-java/src/main/java/com/kisan/shared/security/JwtAuthFilter.java
---
package com.kisan.shared.security;
public class JwtAuthFilter { /* TODO: implement */ }
---

FILE shared/lib-java/src/main/java/com/kisan/shared/error/ProblemDetails.java
---
package com.kisan.shared.error;
public record ProblemDetails(String type, String title, int status, String detail) {}
---

FILE services/identity-svc/src/main/java/com/kisan/identity/IdentityApplication.java
---
package com.kisan.identity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@SpringBootApplication
@RestController
public class IdentityApplication {
  public static void main(String[] args){ SpringApplication.run(IdentityApplication.class,args); }

  @GetMapping("/actuator/health")
  public Map<String,String> health(){ return Map.of("status","UP"); }
}
---

FILE services/identity-svc/src/main/resources/application.yaml
---
server:
  port: 8081
spring:
  application:
    name: identity-svc
---

FILE ml/inference/pyproject.toml
---
[project]
name = "kisan-ml-inference"
version = "0.1.0"
dependencies = ["fastapi","uvicorn"]
---

FILE ml/inference/app/main.py
---
from fastapi import FastAPI
app = FastAPI()
@app.get("/health")
def health():
    return {"status":"UP"}
---

FILE .github/workflows/ci-basic.yml
---
name: ci-basic
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - name: Gradle build
        run: ./gradlew build -x test || true
      - name: Python check
        run: python -m pip install fastapi uvicorn || true
---

END OF FILE LIST. WRITE FILES NOW.
```

---

## 3) M2 — identity-svc OTP→JWT (minimal runnable)
```
SYSTEM: File generator. No explanations.
CONSTRAINTS:
- Only create/modify the listed files.
- Minimal runnable code with tests as stubs.
- If direct file writes fail, emit 'apply.sh' that writes all files.

FILES TO CREATE/UPDATE:
./services/identity-svc/build.gradle.kts
./services/identity-svc/src/main/java/com/kisan/identity/api/AuthController.java
./services/identity-svc/src/main/java/com/kisan/identity/service/OtpService.java
./services/identity-svc/src/main/java/com/kisan/identity/service/JwtService.java
./services/identity-svc/src/test/java/com/kisan/identity/AuthControllerTest.java

REQUIREMENTS:
- POST /auth/otp/send {phone} → 202
- POST /auth/otp/verify {phone, code, device_id} → 200 {access_token, refresh_token}
- HS256 secret 'dev-secret' (env override later), exp: access 15m, refresh 7d
- Tests: one happy-path per endpoint (mock services)
WRITE FILES NOW.
```

---

## 4) M3 — profile-svc (farmer/plot + Redis cache)
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./services/profile-svc/build.gradle.kts
./services/profile-svc/src/main/resources/application.yaml
./services/profile-svc/src/main/resources/db/migration/V1__baseline.sql
./services/profile-svc/src/main/java/com/kisan/profile/ProfileApplication.java
./services/profile-svc/src/main/java/com/kisan/profile/api/FarmerController.java
./services/profile-svc/src/main/java/com/kisan/profile/repo/*.java
TESTS: optional stubs.
REQUIREMENTS:
- Entities: farmer(id, phone, locale, pincode, org_id, land_size, lat, lon), plot(id, farmer_id, area, soil_type, crop, irrigation, lat, lon, season, variety)
- Endpoints: POST/GET /farmers, GET /farmers/{id}
- Redis cache GET by id (TTL 60s)
WRITE FILES NOW.
```

---

## 5) M4 — media-svc, ml/inference, advisory-svc (MVP)
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
# media
./services/media-svc/** (minimal Spring Boot with POST /media/sign returning dummy presigned URLs compatible with MinIO)
# ml inference (update existing FastAPI app)
./ml/inference/app/main.py (POST /infer returns top3 deterministic labels)
# advisory
./services/advisory-svc/** (POST /advisories multipart; persist record; publish Kafka 'advisory.created'; GET by id)
# migrations
./services/advisory-svc/src/main/resources/db/migration/V1__baseline.sql

REQUIREMENTS:
- media-svc validates contentType and returns url+expiresIn (stub ok)
- advisory computes SHA-256 of payload JSON, stores model_version + predictions
- publish Kafka event (stub producer ok), listener calls notary later
WRITE FILES NOW.
```

---

## 6) M5 — notary-svc (mock Fabric + anchor job)
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./services/notary-svc/** (POST /notary/hash; scheduled /anchor/run; simple in-memory or DB mapping)
WRITE FILES NOW.
```

---

## 7) M6 — iot-svc, weather-svc, prices-svc
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./services/iot-svc/** (MQTT consumer stub + GET /iot/readings)
./services/weather-svc/** (GET /weather/forecast with 1h cache)
./services/prices-svc/** (CSV ingest job + GET /prices trend)
WRITE FILES NOW.
```

---

## 8) M7 — forum-svc + notification-svc
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./services/forum-svc/** (threads/posts, moderation_status, toxicity_score; POST /threads, /posts)
./services/notification-svc/** (POST /notify SMS|PUSH; Mailhog stub)
WRITE FILES NOW.
```

---

## 9) M8 — API Gateway + Observability
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./services/gateway/** (Spring Cloud Gateway or Kong declarative config)
./ops/grafana-dashboards/** (basic dashboards)
./ops/otel/** (otel-collector config if using one)
WRITE FILES NOW.
```

---

## 10) M9 — CI + docker-compose health + smoke
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./.github/workflows/ci-full.yml (build/test docker images, simple smoke)
./docker-compose.yml (add healthchecks for services)
./docs/demos/** (use provided smoke scripts from earlier; or generate minimal endpoint checks)
WRITE FILES NOW.
```

---

## 11) M10 — Web PWA (Next.js)
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./web/farmer/** (Next.js 14 + Tailwind; pages: /login, /home, /advisories, /ledger, /schemes, /forum)
./web/farmer/public/manifest.json, icons, sw.js (Workbox)
WRITE FILES NOW.
```

---

## 12) M11 — Admin Console
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./web/admin/** (Next.js + MUI/Tailwind; routes: /dashboard, /farmers, /advisories, /forum/moderation-queue, /schemes, /translations)
WRITE FILES NOW.
```

---

## 13) M12 — Web deploy + CORS/security
```
SYSTEM: File generator. No explanations.
FILES TO CREATE/UPDATE:
./.github/workflows/web-deploy.yml (Vercel/Netlify or S3+CloudFront)
./services/gateway/** (CORS allow farmer/admin domains; rate limits)
./web/** (Security headers via Next middleware)
WRITE FILES NOW.
```

---

## 14) Fallback single-script mode
If Cursor can’t write files directly, paste this tiny prompt:
```
SYSTEM: Emit a single bash script named apply.sh that writes ALL files for the requested milestone using heredocs, runs chmod where needed, and exits with 0. Do not print anything else.
```

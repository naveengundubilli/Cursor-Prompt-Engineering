
# KisanConnect — All-in-One Cursor Prompt Pack (Windows + No-Docker Friendly)

> This version adds **PowerShell smoke scripts** and a **No-Docker mode** so you can develop on Windows without Docker.
> If Cursor cannot write files directly, it must emit a single `apply.ps1` or `apply.sh` script to create them.

---

## 0) Global Low-Verbosity Build Mode (prepend to every prompt)

```
SYSTEM: You are in LOW-VERBOSITY BUILD MODE.
RULES:
1) Write/modify files; do NOT explain.
2) No restatements, diffs, or commentary unless asked for a script.
3) Keep implementations minimal but runnable; add tests only if requested.
4) Prefer small scaffolding scripts and placeholders over huge blobs.
5) Stop after files are written.
Fallback: If you cannot write files directly, output a single 'apply.ps1' (Windows) or 'apply.sh' (bash) that writes all files via heredocs and sets executable bits. Do not output anything else.
```

---

## 1) Windows & No-Docker Guidance

- You can run **Spring Boot services** with an **embedded H2 DB** and **in-memory** stubs (no Postgres/Redis/Kafka required) using the Spring profile: `--spring.profiles.active=nodocker`.
- You can run the **FastAPI** service directly with `uvicorn`.
- Use the **PowerShell smoke scripts** (*.ps1) to exercise endpoints on Windows.

### No-Docker profile expectations (applied per service)
- **DB:** Use `H2` in-memory (`jdbc:h2:mem:<svc>`) and disable Flyway if no migrations are present yet.
- **Cache:** Use Spring `simple` cache (in-memory) instead of Redis.
- **Kafka/MQTT:** Provide **no-op stub publishers/consumers** guarded by a property (e.g., `app.messaging.enabled=false`).
- **External deps (MinIO, SMS, Weather, Prices):** Provide simple stub adapters that return static/dummy data when `nodocker` is active.
- **Media Uploads:** Return a fake pre-signed URL structure (not used to actually upload) when in `nodocker` mode.

---

## 2) M1 — One-shot Bootstrap (monorepo + infra) + Windows PowerShell

> Paste this whole block into Cursor to generate the **monorepo** + **nodocker** Spring profile and **PowerShell smoke**.

```
SYSTEM: Act as a file generator. Do not explain anything.
RESPONSE CONTRACT:
- Write/overwrite the exact files listed below.
- If a file is a shell script, set executable (0755). For PowerShell, use .ps1.
- No prose, no diffs. If direct file writes are impossible, emit a single 'apply.ps1' that writes them.

TASK: Create a minimal, runnable M1 foundation (monorepo + nodocker profiles + Windows scripts). Include docker-compose (optional), but ensure nodocker mode runs without Docker.

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
./scripts/windows/bootstrap_m1.ps1
./scripts/windows/check_m1.ps1

./docker-compose.yml  # optional infra for later

./shared/lib-java/build.gradle.kts
./shared/lib-java/src/main/java/com/kisan/shared/Sentinel.java
./shared/lib-java/src/main/java/com/kisan/shared/security/JwtAuthFilter.java
./shared/lib-java/src/main/java/com/kisan/shared/error/ProblemDetails.java
./shared/contracts/.keep

./services/identity-svc/build.gradle.kts
./services/identity-svc/src/main/java/com/kisan/identity/IdentityApplication.java
./services/identity-svc/src/main/java/com/kisan/identity/api/AuthController.java
./services/identity-svc/src/main/java/com/kisan/identity/service/OtpService.java
./services/identity-svc/src/main/java/com/kisan/identity/service/JwtService.java
./services/identity-svc/src/main/resources/application.yaml
./services/identity-svc/src/main/resources/application-nodocker.yaml

./services/profile-svc/build.gradle.kts
./services/profile-svc/src/main/java/com/kisan/profile/ProfileApplication.java
./services/profile-svc/src/main/java/com/kisan/profile/api/FarmerController.java
./services/profile-svc/src/main/java/com/kisan/profile/repo/FarmerRepo.java
./services/profile-svc/src/main/java/com/kisan/profile/repo/PlotRepo.java
./services/profile-svc/src/main/resources/application.yaml
./services/profile-svc/src/main/resources/application-nodocker.yaml
./services/profile-svc/src/main/resources/db/migration/V1__baseline.sql

./ml/inference/pyproject.toml
./ml/inference/app/main.py
./scripts/windows/run_ml.ps1

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
*.iml
.idea/
---

FILE README.md
---
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

---

FILE settings.gradle.kts
---
rootProject.name = "kisanconnect"
include("shared:lib-java")
include("services:identity-svc")
include("services:profile-svc")
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
# placeholder; run bootstrap script to generate wrapper
---

FILE scripts/windows/bootstrap_m1.ps1
---
Param()

# Ensure gradle wrapper
if (-not (Test-Path "./gradlew.bat")) {
  if (Get-Command gradle -ErrorAction SilentlyContinue) {
    gradle wrapper --gradle-version 8.9
  } else {
    Write-Host "Gradle not found. Install Gradle or run 'gradle wrapper' once available."
  }
}

# Create minimal shared lib build file if missing
$libBuild = "shared/lib-java/build.gradle.kts"
if (-not (Test-Path $libBuild)) {
  New-Item -ItemType Directory -Force -Path "shared/lib-java/src/main/java/com/kisan/shared/security" | Out-Null
  New-Item -ItemType Directory -Force -Path "shared/lib-java/src/main/java/com/kisan/shared/error" | Out-Null
  @"
plugins {
  `java-library`
}
java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
dependencies {
  api("org.slf4j:slf4j-api:2.0.13")
}
"@ | Set-Content -Encoding UTF8 $libBuild
}

Write-Host "Bootstrap complete."
---

FILE scripts/windows/check_m1.ps1
---
Param()
$ErrorActionPreference = "SilentlyContinue"
Write-Host "Check Identity health (expected UP if running):"
try { Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -UseBasicParsing } catch {}
Write-Host "Check Profile health (expected UP if running):"
try { Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -UseBasicParsing } catch {}
Write-Host "Check ML FastAPI health (expected UP if running):"
try { Invoke-RestMethod -Uri "http://localhost:8000/health" -UseBasicParsing } catch {}
---

FILE scripts/bootstrap_m1.sh (chmod 0755)
---
#!/usr/bin/env bash
set -euo pipefail
if ! ./gradlew --version >/dev/null 2>&1; then
  if command -v gradle >/dev/null 2>&1; then
    gradle wrapper --gradle-version 8.9
  else
    echo "Gradle not installed. Install Gradle or run 'gradle wrapper' once available."
  fi
fi
echo "Bootstrap complete."
---

FILE scripts/check_m1.sh (chmod 0755)
---
#!/usr/bin/env bash
set -euo pipefail
curl -s http://localhost:8081/actuator/health || true
curl -s http://localhost:8082/actuator/health || true
curl -s http://localhost:8000/health || true
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
  redis:
    image: redis:7
    ports: ["6379:6379"]
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.3
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
  kafka:
    image: confluentinc/cp-kafka:7.5.3
    depends_on: [zookeeper]
    ports: ["9092:9092"]
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
public class JwtAuthFilter { /* TODO */ }
---

FILE shared/lib-java/src/main/java/com/kisan/shared/error/ProblemDetails.java
---
package com.kisan.shared.error;
public record ProblemDetails(String type, String title, int status, String detail) {}
---

FILE services/identity-svc/build.gradle.kts
---
plugins {
  id("org.springframework.boot") version "3.3.2"
  id("io.spring.dependency-management")
  java
}
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
dependencies {
  implementation(project(":shared:lib-java"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
tasks.test { useJUnitPlatform() }
---

FILE services/identity-svc/src/main/java/com/kisan/identity/IdentityApplication.java
---
package com.kisan.identity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class IdentityApplication {
  public static void main(String[] args){ SpringApplication.run(IdentityApplication.class,args); }
}
---

FILE services/identity-svc/src/main/java/com/kisan/identity/api/AuthController.java
---
package com.kisan.identity.api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth/otp")
public class AuthController {
  @PostMapping("/send")
  public ResponseEntity<?> send(@RequestBody Map<String,String> body){
    return ResponseEntity.accepted().build();
  }
  @PostMapping("/verify")
  public Map<String,String> verify(@RequestBody Map<String,String> body){
    return Map.of("access_token","dev.jwt","refresh_token","dev.rjwt");
  }
}
---

FILE services/identity-svc/src/main/java/com/kisan/identity/service/OtpService.java
---
package com.kisan.identity.service;
public class OtpService {}
---

FILE services/identity-svc/src/main/java/com/kisan/identity/service/JwtService.java
---
package com.kisan.identity.service;
public class JwtService {}
---

FILE services/identity-svc/src/main/resources/application.yaml
---
server:
  port: 8081
spring:
  application:
    name: identity-svc
---

FILE services/identity-svc/src/main/resources/application-nodocker.yaml
---
spring:
  main:
    banner-mode: "off"
app:
  mode: "nodocker"
---

FILE services/profile-svc/build.gradle.kts
---
plugins {
  id("org.springframework.boot") version "3.3.2"
  id("io.spring.dependency-management")
  java
}
java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
dependencies {
  implementation(project(":shared:lib-java"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("com.h2database:h2")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
tasks.test { useJUnitPlatform() }
---

FILE services/profile-svc/src/main/java/com/kisan/profile/ProfileApplication.java
---
package com.kisan.profile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class ProfileApplication {
  public static void main(String[] args){ SpringApplication.run(ProfileApplication.class,args); }
}
---

FILE services/profile-svc/src/main/java/com/kisan/profile/api/FarmerController.java
---
package com.kisan.profile.api;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/farmers")
public class FarmerController {
  @PostMapping public Map<String,Object> upsert(@RequestBody Map<String,Object> farmer){
    if(!farmer.containsKey("id")) farmer.put("id", UUID.randomUUID().toString());
    return farmer;
  }
  @GetMapping("/{id}") public Map<String,Object> byId(@PathVariable String id){
    return Map.of("id", id, "phone", "+910000000001");
  }
  @GetMapping public List<Map<String,Object>> list(){
    return List.of(Map.of("id","1","phone","+910000000001"));
  }
}
---

FILE services/profile-svc/src/main/java/com/kisan/profile/repo/FarmerRepo.java
---
package com.kisan.profile.repo;
public interface FarmerRepo {}
---

FILE services/profile-svc/src/main/java/com/kisan/profile/repo/PlotRepo.java
---
package com.kisan.profile.repo;
public interface PlotRepo {}
---

FILE services/profile-svc/src/main/resources/application.yaml
---
server:
  port: 8082
spring:
  application:
    name: profile-svc
---

FILE services/profile-svc/src/main/resources/application-nodocker.yaml
---
spring:
  datasource:
    url: jdbc:h2:mem:profile;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.H2Dialect
  cache:
    type: simple
---

FILE services/profile-svc/src/main/resources/db/migration/V1__baseline.sql
---
-- placeholder migration (applies when Postgres is used)
-- create table farmer (...);
-- create table plot (...);
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
from pydantic import BaseModel

app = FastAPI()

class InferReq(BaseModel):
    image_url: str | None = None

@app.get("/health")
def health():
    return {"status":"UP"}

@app.post("/infer")
def infer(req: InferReq):
    return {
        "model_version": "stub-0.1",
        "labels": [
            {"label":"aphids","confidence":0.82},
            {"label":"leaf_spot","confidence":0.13},
            {"label":"healthy","confidence":0.05}
        ]
    }
---

FILE scripts/windows/run_ml.ps1
---
Param()
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
---

FILE .github/workflows/ci-basic.yml
---
name: ci-basic
on: [push, pull_request]
jobs:
  build:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - name: Gradle build
        run: ./gradlew.bat build -x test || true
      - name: Python check
        shell: pwsh
        run: |
          pip install fastapi uvicorn
          python - <<'PY'
import sys; print("FastAPI OK")
PY
---

END OF FILE LIST. WRITE FILES NOW.
```

---

## 3) Windows PowerShell Smoke Scripts (M2–M5)

> Paste each block into Cursor when those services exist; it will create `docs\demos_windows\...\smoke.ps1`

### M2 — identity (OTP→JWT stub)
```
SYSTEM: File generator. No explanations.
FILES:
./docs/demos_windows/M2-auth-identity/smoke.ps1
CONTENT:
---
Param()
$body = @{ phone = "+910000000000" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/auth/otp/send" -ContentType "application/json" -Body $body -ErrorAction SilentlyContinue | Out-Null
$verify = @{ phone = "+910000000000"; code = "000000"; device_id = "dev-win-01" } | ConvertTo-Json
$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8081/auth/otp/verify" -ContentType "application/json" -Body $verify
$res
"export ACCESS_TOKEN=$($res.access_token)"
---
WRITE FILES NOW.
```

### M3 — profile
```
SYSTEM: File generator. No explanations.
FILES:
./docs/demos_windows/M3-farmer-profile/smoke.ps1
CONTENT:
---
Param([string]$Token)
if (-not $Token) { Write-Host "Pass -Token <access_token>"; exit 1 }
$headers = @{ Authorization = "Bearer $Token" }
$body = @{ phone = "+910000000001"; locale="hi"; pincode="110001"; land_size=2.5 } | ConvertTo-Json
$res = Invoke-RestMethod -Method Post -Uri "http://localhost:8082/farmers" -Headers $headers -ContentType "application/json" -Body $body
$res
$id = $res.id
Invoke-RestMethod -Method Get -Uri "http://localhost:8082/farmers/$id" -Headers $headers
---
WRITE FILES NOW.
```

### M4 — advisory (stub path only; real impl in later milestones)
```
SYSTEM: File generator. No explanations.
FILES:
./docs/demos_windows/M4-advisory/smoke.ps1
CONTENT:
---
Param([string]$Token)
if (-not $Token) { Write-Host "Pass -Token <access_token>"; exit 1 }
Write-Host "Advisory endpoints pending in later milestone; this is a placeholder."
---
WRITE FILES NOW.
```

### M5 — notary (placeholder)
```
SYSTEM: File generator. No explanations.
FILES:
./docs/demos_windows/M5-notary/smoke.ps1
CONTENT:
---
Param([string]$Token,[string]$AdvisoryId)
Write-Host "Notary placeholder. Will call /notary/hash once implemented."
---
WRITE FILES NOW.
```

---

## 4) Update Later Milestone Prompts to include `nodocker` variants

When you reach M4+ (media/advisory/notary/iot/etc.), **prepend this to each prompt**:

```
Include Spring Boot profile 'nodocker':
- application-nodocker.yaml with H2 datasource and spring.cache.type=simple
- Replace external adapters (Kafka, Redis, S3, MQTT) with no-op or in-memory stubs when 'nodocker' is active
- Provide a configuration property 'app.stub=true' to branch behavior
- Add a PowerShell smoke script under docs/demos_windows/<milestone>/smoke.ps1 to hit the new endpoints
```

That’s it — you can now build and run on Windows with no Docker, and flip to Docker/Desktop later without changing code (just change Spring profiles).


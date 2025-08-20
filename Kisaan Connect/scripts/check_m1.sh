#!/usr/bin/env bash
set -euo pipefail
curl -s http://localhost:8081/actuator/health || true
curl -s http://localhost:8082/actuator/health || true
curl -s http://localhost:8000/health || true


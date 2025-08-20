Param()
$ErrorActionPreference = "SilentlyContinue"
Write-Host "Check Identity health (expected UP if running):"
try { Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -UseBasicParsing } catch {}
Write-Host "Check Profile health (expected UP if running):"
try { Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -UseBasicParsing } catch {}
Write-Host "Check ML FastAPI health (expected UP if running):"
try { Invoke-RestMethod -Uri "http://localhost:8000/health" -UseBasicParsing } catch {}


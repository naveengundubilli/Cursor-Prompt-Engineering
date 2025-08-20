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


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


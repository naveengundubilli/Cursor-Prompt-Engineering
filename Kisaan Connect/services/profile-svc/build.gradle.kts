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


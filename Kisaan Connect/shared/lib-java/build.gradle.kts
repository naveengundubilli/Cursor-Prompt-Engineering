plugins {
  `java-library`
}
java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}
dependencies {
  api("org.slf4j:slf4j-api:2.0.13")
}


plugins {
    application
    java
    id("org.beryx.jlink") version "3.0.1"
    id("org.gradlex.reproducible-builds") version "1.0"
}

repositories {
    if (project.hasProperty("offline")) {
        mavenLocal()
    } else {
        mavenCentral()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("app.Main")
    applicationDefaultJvmArgs = listOf("--add-modules", "javafx.controls,javafx.fxml")
}

dependencies {
    implementation(libs.javafx.controls)
    implementation(libs.javafx.fxml)
    implementation(libs.pdfbox)
    implementation(libs.fontbox)
    implementation(libs.bcprov)
    implementation(libs.bcpkix)
    implementation(libs.tess4j)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback)

    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

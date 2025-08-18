plugins {
	application
	java
	id("org.openjfx.javafxplugin") version "0.1.0"
	id("org.owasp.dependencycheck") version "9.0.9"
	id("com.github.spotbugs") version "6.0.20"
}

version = "1.0.0"

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

javafx {
	version = "17.0.2"
	modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base", "javafx.swing")
}

application {
	mainClass.set("com.securepdfeditor.App")
}

dependencies {
	implementation(libs.pdfbox)
	implementation(libs.fontbox)
	implementation(libs.bcprov)
	implementation(libs.bcpkix)
	implementation(libs.tess4j)
	implementation(libs.slf4j.api)
	runtimeOnly(libs.logback)

	testImplementation(platform("org.junit:junit-bom:5.11.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	// Ensure JUnit Platform launcher available on test runtime
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()
	maxHeapSize = "2048m"
	jvmArgs = listOf("-XX:+HeapDumpOnOutOfMemoryError", "-XX:MaxRAMPercentage=75")
	maxParallelForks = 1
}

// Custom packaging tasks for Milestone 5
tasks.register("packageApp") {
	dependsOn("jar")
	description = "Create application package"
	group = "distribution"
	
	doLast {
		val distDir = file("dist")
		distDir.mkdirs()
		
		val platform = when {
			System.getProperty("os.name").lowercase().contains("windows") -> "windows"
			System.getProperty("os.name").lowercase().contains("mac") -> "macos"
			else -> "linux"
		}
		
		val packageName = "secure-pdf-editor-${project.version}-$platform"
		val packageFile = distDir.resolve("$packageName.zip")
		
		// Create simple package with jar and resources
		ant.withGroovyBuilder {
			"zip"(mapOf(
				"destfile" to packageFile
			)) {
				"fileset"(mapOf("dir" to "build/libs")) {
					"include"(mapOf("name" to "*.jar"))
				}
				"fileset"(mapOf("dir" to "src/main/resources")) {
					"include"(mapOf("name" to "**/*"))
				}
				"fileset"(mapOf("dir" to ".")) {
					"include"(mapOf("name" to "gradlew*"))
					"include"(mapOf("name" to "build.gradle.kts"))
					"include"(mapOf("name" to "settings.gradle.kts"))
					"include"(mapOf("name" to "libs.versions.toml"))
					"include"(mapOf("name" to "LICENSE"))
					"include"(mapOf("name" to "README.md"))
				}
			}
		}
		
		println("Package created: $packageFile")
	}
}

// Custom task for building all packages
tasks.register("buildAllPackages") {
    dependsOn("packageApp")
    description = "Build all platform packages"
    group = "distribution"
}

// SpotBugs configuration
spotbugs {
    toolVersion.set("4.8.4")
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") {
        required.set(true)
        outputLocation.set(layout.buildDirectory.file("reports/spotbugs/${name}.html"))
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.register("quality") {
    group = "verification"
    description = "Run unit tests, SpotBugs and OWASP Dependency Check"
    dependsOn("test", "spotbugsMain", "spotbugsTest", "dependencyCheckAnalyze")
}

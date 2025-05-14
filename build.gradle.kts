import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val javaVersion = JavaLanguageVersion.of(21)
val springdocVersion = "2.8.6"
val tilleggsstønaderLibsVersion = "2025.04.22-12.35.62d0c031bca8"
val tilleggsstønaderKontrakterVersion = "2025.05.14-19.35.860d7aa9a3ab"
val tokenSupportVersion = "5.0.25"
val springCloudVersion = "4.2.1"

val mockkVersion = "1.14.0"

group = "no.nav.tilleggsstonader.integrasjoner"
version = "1.0.0"

plugins {
    application

    kotlin("jvm") version "2.1.20"
    id("com.diffplug.spotless") version "7.0.3"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18"

    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.spring") version "2.1.20"

    id("org.cyclonedx.bom") version "2.2.0"
}

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

apply(plugin = "com.diffplug.spotless")

spotless {
    kotlin {
        ktlint("1.5.0")
    }
}

configurations.all {
    resolutionStrategy {
        failOnNonReproducibleResolution()
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-common:$springdocVersion")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    implementation("io.micrometer:micrometer-registry-prometheus")

    // Tillegggsstønader libs
    implementation("no.nav.tilleggsstonader-libs:util:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:log:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:http-client:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:sikkerhet:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:spring:$tilleggsstønaderLibsVersion")

    implementation("no.nav.tilleggsstonader.kontrakter:tilleggsstonader-kontrakter:$tilleggsstønaderKontrakterVersion")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:$springCloudVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")

    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("no.nav.tilleggsstonader-libs:test-util:$tilleggsstønaderLibsVersion")
}

kotlin {
    jvmToolchain(javaVersion.asInt())

    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

application {
    mainClass.set("no.nav.tilleggsstonader.integrasjoner.AppKt")
}

if (project.hasProperty("skipLint")) {
    gradle.startParameter.excludedTaskNames += "spotlessKotlinCheck"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = false
        showCauses = false
    }
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

tasks.cyclonedxBom {
    setIncludeConfigs(listOf("runtimeClasspath", "compileClasspath"))
}

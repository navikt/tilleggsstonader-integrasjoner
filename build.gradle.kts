val javaVersion = JavaLanguageVersion.of(17)
val springdocVersion = "2.1.0"
val tilleggsstønaderLibsVersion = "2023.09.14-10.25.400ea92abb53"
val tilleggsstønaderKontrakterVersion = "2023.09.15-11.10.b0d5e04e99dc"
val tokenSupportVersion = "3.1.5"
val wiremockVersion = "2.35.0"
val springCloudVersion = "4.0.4"
val mockkVersion = "1.13.7"
val testcontainerVersion = "1.19.0"

group = "no.nav.tilleggsstonader.integrasjoner"
version = "1.0.0"

plugins {
    application

    kotlin("jvm") version "1.9.10"
    id("com.diffplug.spotless") version "6.21.0"
    id("com.github.ben-manes.versions") version "0.47.0"

    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("plugin.spring") version "1.9.10"

    id("org.cyclonedx.bom") version "1.7.4"
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
        ktlint("0.50.0")
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
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    implementation("io.micrometer:micrometer-registry-prometheus")

    // Tillegggsstønader libs
    implementation("no.nav.tilleggsstonader-libs:util:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:log:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:http-client:$tilleggsstønaderLibsVersion")
    implementation("no.nav.tilleggsstonader-libs:sikkerhet:$tilleggsstønaderLibsVersion")

    implementation("no.nav.tilleggsstonader.kontrakter:tilleggsstonader-kontrakter:$tilleggsstønaderKontrakterVersion")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:$springCloudVersion")

    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:$wiremockVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
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
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

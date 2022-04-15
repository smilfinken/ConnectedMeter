import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.allopen") version "1.6.20"
    kotlin("plugin.jpa") version "1.6.20"
    kotlin("plugin.noarg") version "1.6.20"
    kotlin("plugin.spring") version "1.6.20"

    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "net.smilfinken.meter"
version = "0.2.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // spring boot stuff
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // kotlin things
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // nice to have
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.github.snksoft:crc:1.0.2")
    implementation("com.google.code.gson:gson:2.9.0")

    // runtime in-memory database
    runtimeOnly("com.h2database:h2")

    // testing tools
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}
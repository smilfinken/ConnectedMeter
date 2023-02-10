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
version = "0.8.3"
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
    implementation("org.apache.commons:commons-lang3:3.12.0")

    // production database
    runtimeOnly("org.postgresql:postgresql:42.3.3")

    // runtime in-memory database
    runtimeOnly("com.h2database:h2")

    // compileOnly("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    // testing tools
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
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
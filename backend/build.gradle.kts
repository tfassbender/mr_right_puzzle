plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

val quarkusPlatformVersion = providers.gradleProperty("quarkusPlatformVersion").get()

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    // Generated puzzles are kept in memory, keyed by seed + config (only those are stored).
    implementation("io.quarkus:quarkus-cache")
    // Kotlin data classes (de)serialize without no-arg constructors.
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Auth: HS256 JWT issue + verify, transactional email, BCrypt password hashing.
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    implementation("io.quarkus:quarkus-mailer")
    implementation("at.favre.lib:bcrypt:0.10.2")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.tngtech.archunit:archunit:1.4.1")
}

group = "com.tfassbender.mrright"
version = "1.0.0"

// Quarkus needs CDI beans, REST resources and test classes to be non-final.
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        // Quarkus/Jackson rely on parameter names (the Java analog is -parameters).
        javaParameters = true
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

// buildFrontend (root project) copies the SPA into src/main/resources; when both run, copy first.
// mustRunAfter instead of dependsOn, so plain backend builds and tests don't need npm.
tasks.named("processResources") {
    mustRunAfter(":buildFrontend")
}

// The packaged JAR is the deliverable and serves the SPA, so packaging always builds the current frontend.
tasks.named("quarkusBuild") {
    dependsOn(":buildFrontend")
}

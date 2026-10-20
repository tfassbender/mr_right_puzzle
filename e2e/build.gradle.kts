plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("com.microsoft.playwright:playwright:1.50.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    // Gradle 9 no longer puts the JUnit Platform launcher on the test runtime classpath implicitly.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.test {
    useJUnitPlatform()
    // Only run explicitly (./gradlew :e2e:e2eTest), never as part of a normal `build`/`check`.
    onlyIf { gradle.taskGraph.hasTask(":e2e:e2eTest") }
    // Default: headless. Pass -Pheadless=false to watch the browser.
    systemProperty("playwright.headless", findProperty("headless")?.toString() ?: "true")
    // Frontend under test (Vite dev server by default).
    systemProperty("mrright.e2e.base-url", findProperty("baseUrl")?.toString() ?: "http://localhost:5174")
    // Browsers are installed explicitly with installBrowsers.
    environment("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")
}

tasks.register("e2eTest") {
    group = "e2e"
    description = "Run e2e tests (requires backend on :4720 and frontend dev server on :5174)"
    dependsOn("test")
}

// Start both dev servers in separate terminal windows (returns immediately).
tasks.register("startE2eServers") {
    group = "e2e"
    description = "Open backend (quarkusDev) and frontend dev server each in a new terminal window"
    doLast {
        val rootDir: String = rootProject.projectDir.absolutePath
        val frontendDir = "$rootDir\\frontend"

        ProcessBuilder(
            "cmd", "/c",
            """start "Mr Right Backend" cmd /k "cd /d $rootDir && gradlew.bat :backend:quarkusDev""""
        ).start()

        ProcessBuilder(
            "cmd", "/c",
            """start "Mr Right Frontend" cmd /k "cd /d $frontendDir && npm run dev""""
        ).start()

        println("Two terminal windows opened.")
        println("Wait for both servers to be ready, then run:  ./gradlew :e2e:e2eTest")
    }
}

// Install the Playwright Chromium binary (run once after the initial setup).
tasks.register<JavaExec>("installBrowsers") {
    group = "playwright"
    description = "Download the Chromium browser binary for Playwright (run once)"
    classpath = configurations.testRuntimeClasspath.get()
    mainClass.set("com.microsoft.playwright.CLI")
    args = listOf("install", "chromium")
}

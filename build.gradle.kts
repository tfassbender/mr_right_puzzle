group = "com.tfassbender.mrright"
version = "0.0.1"

// Builds the SPA and copies it into the backend's classpath, so Quarkus serves it from the single JAR.
// :backend:quarkusBuild depends on it, so ./gradlew :backend:quarkusBuild always packages the current SPA.
tasks.register<Copy>("buildFrontend") {
    group = "build"
    description = "Build the Preact SPA and copy it into the backend static resources"
    dependsOn(":frontend:npmBuild")
    from("frontend/dist")
    into("backend/src/main/resources/META-INF/resources")
}

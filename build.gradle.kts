plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    jacoco
    checkstyle
    pmd
    // Spotless is optional; CI guards existence but we keep it enabled for formatting
    id("com.diffplug.spotless") version "6.25.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    mavenLocal()
}

dependencies {
    // Spring Boot core + web (safe to include; resolves @SpringBootApplication and web imports)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Google API client stack (HTTP + JSON + OAuth helpers)
    implementation("com.google.api-client:google-api-client:2.6.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")
    // OkHttp for HTTP requests
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Twitter4J for X/Twitter interactions
    implementation("org.twitter4j:twitter4j-core:4.0.7")

    // If code imports com.google.api.services.drive.Drive, include Drive service:
    implementation("com.google.apis:google-api-services-drive:v3-rev20230815-2.0.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "10.17.0"
    // Use bundled default if no config provided
    isShowViolations = true
}

pmd {
    toolVersion = "6.55.0"
    isConsoleOutput = true
    ruleSets = listOf("category/java/bestpractices.xml", "category/java/errorprone.xml")
}

jacoco {
    toolVersion = "0.8.12"
}

spotless {
    java {
        googleJavaFormat()  // safe default
        target("**/*.java")
    }
}

tasks.register("staticChecks") {
    group = "verification"
    description = "Run Checkstyle and PMD on main and test sources"
    dependsOn("checkstyleMain", "checkstyleTest", "pmdMain", "pmdTest")
}

// Make `build` run style + static first, but don’t block compilation if they fail locally.
// CI already guards and logs; keep local dev fast.
tasks.named("build") {
    dependsOn("spotlessApply", "staticChecks")
}

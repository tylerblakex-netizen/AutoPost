plugins {
    java
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
    // TODO: Add real deps here as compile errors appear in CI (implementation("group:artifact:version"))
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
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

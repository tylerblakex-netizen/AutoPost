plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // JUnit 5 for unit tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

/**
 * Lightweight integrationTest source set
 * - Runs only in CI's integration job
 * - Test can skip itself if secrets/env are not present
 */
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
}

configurations {
    val integrationTestImplementation by creating {
        extendsFrom(configurations.testImplementation.get())
    }
    val integrationTestRuntimeOnly by creating {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

dependencies {
    add("integrationTestImplementation", "org.junit.jupiter:junit-jupiter:5.10.0")
    add("integrationTestRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration smoke tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    // Keep memory sane on CI
    jvmArgs("-Xmx1g")
    // Show skipped tests too
    reports.html.required.set(true)
}

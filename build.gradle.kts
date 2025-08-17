plugins { java }

repositories { mavenCentral() }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

dependencies {
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed", "skipped", "passed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// Integration source set so the CI step always exists
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
}
configurations {
    val integrationTestImplementation by creating { extendsFrom(configurations.testImplementation.get()) }
    val integrationTestRuntimeOnly by creating { extendsFrom(configurations.testRuntimeOnly.get()) }
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
    jvmArgs("-Xmx1g")
    testLogging {
        events("failed", "skipped", "passed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

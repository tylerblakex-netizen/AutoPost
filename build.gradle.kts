plugins {
    // If you don't use Kotlin, remove this next line.
    kotlin("jvm") version "1.9.24" apply false
    // Java plugin guarantees 'test' & 'build' tasks exist
    java
}

repositories {
    mavenCentral()
    google()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}

// If you DO use Kotlin sources, uncomment to apply Kotlin on the root/module:
// apply(plugin = "org.jetbrains.kotlin.jvm")
// dependencies { testImplementation(kotlin("test")) }

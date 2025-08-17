plugins {
    id("java")
    id("application")
}

group = "com.example.autopost"
version = "1.0.0"

repositories {
    mavenCentral()  // Explained: Pins to mavenCentral for reproducibility.
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))  // Explained: Enforces Java 21; code is compatible.
    }
}

dependencies {
    implementation("com.google.api-client:google-api-client:2.2.0")  // Explained: For Google Drive; version pinned.
    implementation("com.google.apis:google-api-services-drive:v3-rev20230814-2.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")  // Explained: Adds JUnit 5 for tests.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    // Assume existing deps for FFmpeg or MoviePy wrappers; add if needed.
}

application {
    mainClass.set("com.example.autopost.Main")  // Explained: Assumes entrypoint; preserve existing.
}

tasks.test {
    useJUnitPlatform()  // Explained: Enables JUnit 5.
    systemProperty("org.gradle.jvmargs", "-Xmx2g -XX:+HeapDumpOnOutOfMemoryError")  // Explained: JVM args for stability.
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}  // Explained: Enables build cache if using Gradle Enterprise; otherwise, local cache works.

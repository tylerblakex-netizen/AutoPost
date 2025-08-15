plugins {
    java
    jacoco
    checkstyle
    pmd
    id("com.diffplug.spotless") version "6.25.0"
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.6"
}

group = "net.tylerblakex"
version = "0.0.0-SNAPSHOT"

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
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.2"))
    implementation("org.springframework.boot:spring-boot-starter")
    // implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.twitter4j:twitter4j-core:4.0.7")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

checkstyle {
    toolVersion = "10.17.0"
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
        googleJavaFormat()
        target("**/*.java")
    }
}

tasks.register("staticChecks") {
    group = "verification"
    description = "Run Checkstyle and PMD on main and test sources"
    dependsOn("checkstyleMain", "checkstyleTest", "pmdMain", "pmdTest")
}

tasks.named("build") {
    dependsOn("spotlessApply", "staticChecks")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.register("classesOnly") {
    dependsOn("classes")
}

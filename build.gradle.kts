plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.6"
    `java`
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

group = "com.autopost"
version = "1.1.0"

repositories {
    mavenCentral()
    // If you consume private packages from GitHub Packages, uncomment and keep wildcards:
    // maven {
    //     url = uri("https://maven.pkg.github.com/tylerblakex-netizen/*")
    //     credentials {
    //         username = System.getenv("GITHUB_ACTOR") ?: ""
    //         password = System.getenv("GITHUB_TOKEN") ?: ""
    //     }
    // }
}

/*
 * ===== Dependencies (auto-converted from pom.xml) =====
 * Rules:
 * - For Spring Boot-managed artifacts (e.g., spring-boot-starter-*), omit versions.
 * - Map Maven scopes:
 *     compile → implementation
 *     runtime → runtimeOnly
 *     provided → compileOnly
 *     test → testImplementation
 * - Preserve any explicitly pinned versions that are NOT managed by Spring BOM.
 */
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev20250723-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("org.twitter4j:twitter4j-core:4.0.7")
    implementation("org.threeten:threetenbp:1.6.8")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

springBoot {
    // Set if MAIN_CLASS was detected; otherwise omit.
    // MAIN_CLASS is the @SpringBootApplication entrypoint.
    // Remove this block if not resolvable.
    mainClass.set("com.autopost.AutoPostApplication")
}

/*
 * ===== Publishing to GitHub Packages =====
 * Publishes plain Java components (jar + sources + javadoc).
 * spring-boot:bootJar still builds the runnable app jar for releases.
 */
publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            pom {
                name.set("AutoPost")
                description.set("Auto posting utilities / app")
                url.set("https://github.com/tylerblakex-netizen/AutoPost")
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tylerblakex-netizen/AutoPost")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}

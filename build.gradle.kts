plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("pl.wendigo:chrome-reactive-kotlin:0.6+")
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            run {
                groupId = "com.seansoper"
                artifactId = "batil"
                version = "1.0.0"
            }
        }
    }
}

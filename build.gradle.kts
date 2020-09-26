plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
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

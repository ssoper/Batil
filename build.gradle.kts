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
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.11.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("pl.wendigo:chrome-reactive-kotlin:0.6+")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
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

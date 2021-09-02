val buildGroupId = "com.seansoper"
val buildArtifactId = "batil"
val buildVersion = "1.0.0"
val buildJvmTarget = "11"

plugins {
    kotlin("jvm") version "1.5.30"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.4")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("pl.wendigo:chrome-reactive-kotlin:0.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.1")
}

tasks.jar {
    manifest {
        attributes(mapOf(
            "Main-Class" to "$buildGroupId.$buildArtifactId.Core",
            "Manifest-Version" to "1.0",
            "Implementation-Version" to buildVersion))
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.compileKotlin {
    this.kotlinOptions.jvmTarget = buildJvmTarget
}

tasks.compileTestKotlin {
    this.kotlinOptions.jvmTarget = buildJvmTarget
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            run {
                groupId = buildGroupId
                artifactId = buildArtifactId
                version = buildVersion
            }
        }
    }
}

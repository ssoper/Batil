val buildGroupId = "com.seansoper"
val buildArtifactId = "batil"
val buildVersion = "1.0.0"
val buildJvmTarget = "11"

plugins {
    kotlin("jvm") version "1.5.30"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.5.0"
    id("org.jmailen.kotlinter") version "3.6.0"
    id("jacoco")
}

// Temporary fix until Jacoco default version is 0.8.7+
// https://github.com/jacoco/jacoco/issues/1155
allprojects {
    jacoco {
        toolVersion = "0.8.7"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.5")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation("pl.wendigo:chrome-reactive-kotlin:0.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.1")
    testImplementation("io.mockk:mockk:1.12.0")
}

val clientsImplementation by configurations.creating {
    extendsFrom(configurations.compileClasspath.get())
}

dependencies {
    clientsImplementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
}

sourceSets {
    create("clients") {
        compileClasspath += main.get().output
        runtimeClasspath += main.get().output
    }
}

tasks.register<Jar>("fatJar") {
    description = "Create an executable JAR with a command-line client"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveClassifier.set("etrade")

    from(sourceSets.main.get().output, sourceSets["clients"].output)
    dependsOn(configurations.runtimeClasspath)

    manifest.attributes.set("Main-Class", "com.seansoper.batil.clients.Etrade")

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    from({
        sourceSets["clients"].compileClasspath.files
            .filter { it.name.endsWith("jar") }
            .filter { it.name.contains("kotlinx") }
            .map { zipTree(it) }
    })
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

tasks.jacocoTestReport {
    executionData("$buildDir/jacoco/test.exec")
    reports {
        xml.isEnabled = true
        html.isEnabled = true
        xml.destination = File("$buildDir/reports/jacoco/report.xml")
    }
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Batil")
            jdkVersion.set(buildJvmTarget.toInt())
            includes.from("package.md")
            samples.from("src/main/kotlin/Samples.kt")

            sourceLink {
                localDirectory.set(file("./src/main/kotlin"))
                remoteUrl.set(uri("https://github.com/ssoper/Batil/tree/master/src/main/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
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

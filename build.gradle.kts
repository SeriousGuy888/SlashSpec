import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

/*
 * This file was generated by the Gradle 'init' task.
 */

group = "io.github.seriousguy888"
version = "1.0.0"
description = "SlashSpec"
java.sourceCompatibility = JavaVersion.VERSION_17

application {
    val name = "io.github.seriousguy888.slashspec"
    mainClass.set(name)
}

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application")
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    // the jar remains up to date even when changing excludes
    // https://github.com/johnrengelman/shadow/issues/62
    outputs.upToDateWhen { false }
    group = "Build"
    description = "Creates a fat jar"
    archiveFileName = "${archiveBaseName.get()}-${version}.jar"
    isReproducibleFileOrder = true
    from(sourceSets.main.get().output)
    from(project.configurations.runtimeClasspath)
    // Excluding these helps shrink our binary dramatically
    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_module")
    exclude("META-INF/maven/**")
}

repositories {
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/repositories/central")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:2.10.1")

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
//    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<ShadowJar> {
    minimize()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

kotlin {
    jvmToolchain(17)
}
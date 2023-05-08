plugins {
    kotlin("jvm") version "1.8.0"
    id("java-library")
    id("maven-publish")
}

group = "io.github.salat-23"
version = "1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
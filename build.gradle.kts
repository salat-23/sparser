import org.apache.tools.ant.taskdefs.Javadoc
import org.gradle.kotlin.dsl.archives
import org.gradle.kotlin.dsl.java

plugins {
    kotlin("jvm") version "1.8.0"
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "io.github.salat-23"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val sourceJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

kotlin {
    jvmToolchain(11)
}

val env = mutableMapOf<String, String>()
file(".env").readLines().forEach { line ->
    val arr = line.split("=")
    env += arr[0] to arr[1]
}

//task javadocJar(type: Jar) {
//    classifier = 'javadoc'
//    from javadoc
//}
//
//task sourcesJar(type: Jar) {
//    classifier = 'sources'
//    from sourceSets.main.allSource
//}

val mvnCentralUsername: String = env["MVN_USERNAME"]!!
val mvnCentralPassword: String = env["MVN_PASSWORD"]!!

publishing {
    repositories {
        maven {
            name = "Release"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            setUrl(if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releasesRepoUrl)
            credentials {
                username = mvnCentralUsername
                password = mvnCentralPassword
            }
        }
    }

    publications {
        create<MavenPublication>("Maven") {
            groupId = "io.github.salat-23"
            artifactId = "sparser"
            version = "1.0.0"
            from(components["kotlin"])
        }
        withType<MavenPublication> {
            artifact(javadocJar)
            artifact(sourceJar)
            pom {
                packaging = "jar"
                name.set("sparser")
                description.set("Advanced string to object parser")
                url.set("io.github.salat-23")
                licenses {
                    license {
                        name.set("GNU LGP LICENSE")
                        url.set("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/salat-23/sparser/issues")
                }
                scm {
                    connection.set("scm:git:git://github.com/salat-23/sparser.git")
                    developerConnection.set("scm:git:git://github.com/salat-23/sparser.git")
                    url.set("https://github.com/salat-23/sparser")
                }
                developers {
                    developer {
                        name.set("salat23")
                        email.set("vorobev.timur.art@gmail.com")
                    }
                }
            }
        }
    }
}

project.ext["signing.keyId"] = env["KEY_ID"]!!
project.ext["signing.password"] = env["PASSWORD"]!!
project.ext["signing.secretKeyRingFile"] = env["SECRET_KEY"]!!
signing {
    sign(configurations.archives.get())
    sign(publishing.publications)
    sign(sourceJar.get())
}

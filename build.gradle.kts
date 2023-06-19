import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    kotlin("jvm") version "1.7.20" apply false
    id("com.google.protobuf") version "0.9.2" apply false
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("com.google.cloud.artifactregistry.gradle-plugin") version "2.2.0"
}

group = "ai.qanda"
if (project.hasProperty("releaseVersion")) {
    version = project.property("releaseVersion") as String
}

ext["grpcJavaVersion"] = "1.54.0"
ext["grpcKotlinVersion"] = "1.3.0"
ext["protobufVersion"] = "3.22.2"
ext["coroutinesVersion"] = "1.6.2"
ext["kotlinPoetVersion"] = "1.12.0"

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "com.google.cloud.artifactregistry.gradle-plugin")
}

subprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.google.protobuf")
        plugin("maven-publish")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                pom {
                    url.set("https://github.com/mscheong01/krotoDC")
                    licenses {
                        license {
                            name.set("Apache 2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("mscheong01")
                            name.set("Minsoo Cheong")
                            email.set("icycle0409@snu.ac.kr")
                            organization.set("MinsooCheong")
                            organizationUrl.set("https://github.com/mscheong01")
                        }
                    }
                    scm {
                        connection.set("scm:git:git@github.com:mscheong01/krotoDC.git")
                        developerConnection.set("scm:git:git@github.com:mscheong01/krotoDC.git")
                        url.set("https://github.com/mscheong01/krotoDC")
                    }
                }
            }
        }

        repositories {
            maven {
                version = version
                name = "qanda-packages"
                url = uri("artifactregistry://asia-northeast3-maven.pkg.dev/mp-artifact-registry-aa49/qanda-packages")
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        ktlint {
            filter {
                exclude { entry ->
                    entry.file.toString().contains("generated")
                }
            }
        }
    }
}

task("updateVersion") {
    properties["releaseVersion"]?.let { releaseVersion ->
        val newSnapshotVersion = (releaseVersion as String).split(".").let {
            "${it[0]}.${it[1].toInt() + 1}.0-SNAPSHOT"
        }

        val file = File(rootDir, "gradle.properties")
        val prop = Properties().apply { load(FileInputStream(file)) }
        if (prop.getProperty("version") != newSnapshotVersion) {
            prop.setProperty("version", newSnapshotVersion)
            prop.store(FileOutputStream(file), null)
        }
    }
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
}

group = "io.github.ezermackenzie"
version = "0.1.0"

kotlin {
    explicitApi()

    jvm()
    androidLibrary {
        namespace = "com.ezermackenzie.rut"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            // Pure Kotlin Multiplatform - no third-party dependencies required for core
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "rut-validator-kmp", version.toString())

    pom {
        name = "RUT Validator KMP"
        description = "High-performance, pure Kotlin Multiplatform Chilean RUT (RUN) validator, formatter, and parser."
        inceptionYear = "2026"
        url = "https://github.com/ezer-mackenzie/rut-validator-kmp"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "ezer-mackenzie"
                name = "Eli-ezer Reuven Ramirez Ruiz"
                email = "ramirez.ruiz.eliezer.reuven@gmail.com"
                url = "https://github.com/ezer-mackenzie"
            }
        }
        scm {
            url = "https://github.com/ezer-mackenzie/rut-validator-kmp"
            connection = "scm:git:git://github.com/ezer-mackenzie/rut-validator-kmp.git"
            developerConnection = "scm:git:ssh://git@github.com/ezer-mackenzie/rut-validator-kmp.git"
        }
    }
}


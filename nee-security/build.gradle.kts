import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(project(":nee-core"))
    implementation(project(":nee-serialization"))
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation(Libs.Kotlin.reflect)
    implementation(Libs.Kotlin.serialization)
    implementation(Libs.Vavr.kotlin) {
            exclude("org.jetbrains.kotlin")
    }
    implementation(Libs.Ktor.clientCore)
    implementation(Libs.Ktor.clientSerialization)
    implementation(Libs.Ktor.clientJsonJvm)

    implementation(Libs.Kotlin.coroutinesJdk8)
    api("io.fusionauth:fusionauth-jwt:4.0.1")

    testImplementation (Libs.Kotest.runnerJunit5Jvm)
    testImplementation(Libs.Ktor.clientMockJvm)

    implementation (Libs.Hoplite.core)
    implementation (Libs.Hoplite.yaml)

    testImplementation(Libs.Slf4J.logback)
}

apply(from = "../publish-mpp.gradle.kts")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
}

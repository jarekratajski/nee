import org.jetbrains.kotlin.config.KotlinCompilerVersion


plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))

    api(project(":nee-core"))
    api(project(":nee-jdbc"))
    implementation(project(":nee-security"))
    implementation(project(":nee-serialization"))
    implementation(project(":nee-security-jdbc"))
    implementation(project(":nee-cache-caffeine"))

    implementation(Libs.Ktor.serverCore)
    implementation(Libs.Ktor.serialization)

    testImplementation(project (":nee-test:nee-security-jdbc-test"))
    testImplementation(Libs.Ktor.serverTestHost)
    testImplementation(Libs.Ktor.clientMockJvm)
    testImplementation(Libs.Ktor.clientSerialization)
    testImplementation(Libs.Kotest.runnerJunit5Jvm)


}
apply(from = "../publish-mpp.gradle.kts")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
}

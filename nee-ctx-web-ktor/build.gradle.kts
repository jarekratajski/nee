import org.jetbrains.kotlin.config.KotlinCompilerVersion


plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))

    api(project(":nee-core"))
    api(project(":nee-jdbc"))
    implementation(project(":nee-security"))
    implementation(project(":nee-security-jdbc"))
    implementation(project(":nee-cache-caffeine"))

    implementation(Libs.Ktor.serverCore)
    implementation(Libs.Vavr.jackson)
    implementation(Libs.Jackson.jacksonModuleKotlin)

    testImplementation(project (":nee-test:nee-security-jdbc-test"))
    testImplementation(Libs.Ktor.serverTestHost)
    testImplementation(Libs.Kotest.runnerJunit5Jvm)

}
apply(from = "../publish-mpp.gradle.kts")



dependencies {
    api (Libs.Vavr.kotlin) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(Libs.Kotlin.serialization)
}


apply(from = "../publish-mpp.gradle.kts")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
}

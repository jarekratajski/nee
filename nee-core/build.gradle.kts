

dependencies {
    api (Libs.Vavr.kotlin) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(Libs.Haste.haste)
    testImplementation (Libs.Kotest.runnerJunit5Jvm)
    testImplementation (Libs.Kotest.assertionsCoreJvm)
    implementation(Libs.Jackson.jacksonAnnotations)
    implementation (Libs.Kotlin.kotlinStdLib)
}


apply(from = "../publish-mpp.gradle.kts")

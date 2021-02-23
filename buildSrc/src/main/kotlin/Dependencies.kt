
object Libs {
    const val kotlin_version = "1.4.30"

    object H2 {
        private const val version = "1.4.193"
        const val  h2 = "com.h2database:h2:$version"
    }

    object Kotlin {
        const val version = kotlin_version
        const val kotlinStdLib =  "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
        private const val serializationVersion = "1.1.0-RC"
        private const val coroutinesVersion = "1.4.1"
        const val  coroutinesJdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion"
    }

    object Ktor {
        private const val version = "1.5.1"
        const val serverCore = "io.ktor:ktor-server-core:$version"
        const val clientCore =  "io.ktor:ktor-client-core:$version"
        const val clientMockJvm =  "io.ktor:ktor-client-mock-jvm:$version"
        const val clientJsonJvm =  "io.ktor:ktor-client-json-jvm:$version"
        const val clientJson =  "io.ktor:ktor-client-json:$version"
        const val serialization = "io.ktor:ktor-serialization:$version"
        const val clientSerialization = "io.ktor:ktor-client-serialization:$version"
        //const val jackson =  "io.ktor:ktor-jackson:$version"
        const val serverTestHost ="io.ktor:ktor-server-test-host:$version"
    }

    object Vavr {
        private const val version = "0.10.2"
        const val kotlin = "io.vavr:vavr-kotlin:$version"
        const val jackson = "io.vavr:vavr-jackson:$version"
    }

    object Haste {
        private const val version = "0.3.1"
        const val haste = "io.github.krasnoludkolo:haste:$version"
    }

    object Kotest {
        private const val version = "4.3.1"
        const val runnerJunit5Jvm ="io.kotest:kotest-runner-junit5-jvm:$version"
        const val assertionsCoreJvm = "io.kotest:kotest-assertions-core-jvm:$version"
        //const val runnerConsoleJvm = "io.kotest:kotest-runner-console-jvm:${consoleVersion}"
    }

    object Slf4J {
        private const val version = "1.7.28"
        private const val logbackVersion = "1.2.3"
        const val api =  "org.slf4j:slf4j-api:$version"
        const val logback =  "ch.qos.logback:logback-classic:$logbackVersion"
    }


    object Liquibase {
        private const val version = "3.6.1"
        const val core = "org.liquibase:liquibase-core:$version"
    }

    object Hoplite {
        private const val version = "1.3.10"
        const val core = "com.sksamuel.hoplite:hoplite-core:$version"
        const val yaml = "com.sksamuel.hoplite:hoplite-yaml:$version"

    }
}

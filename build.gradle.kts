import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.4.0"))
    }
}

plugins {
    java
    id("io.gitlab.arturbosch.detekt").version("1.5.0")
    `kotlin-dsl` //TODO - read about it
    id("jacoco")
    id("maven-publish")
    id("java-library")
    signing
    id("org.jetbrains.dokka") version "0.10.1"
    id("com.bmuschko.nexus") version "2.3.1"
}

repositories {
    jcenter()
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "jacoco")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    group = "pl.setblack"
    version = Ci.publishVersion

    dependencies {
        // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
        implementation(Libs.Slf4J.api)
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions.apply {
        jvmTarget = "1.8"
        javaParameters = true
        allWarningsAsErrors = true
    }

    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions.apply {
        jvmTarget = "1.8"
        javaParameters = true
        allWarningsAsErrors = false
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.jacocoTestReport {
        reports {
            html.isEnabled = true
            xml.isEnabled = false
            csv.isEnabled = false
        }
    }
    //co za wój?
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    detekt {
        failFast = true // fail build on any finding
        buildUponDefaultConfig = true // preconfigure defaults
        config = files("${rootDir}/config/detekt.yml")
        //baseline = file("$projectDir/config/baseline.xml")
        reports {
            html.enabled = true // observe findings in your browser with structure and code snippets
            xml.enabled = true // check(style like format mainly for integrations like Jenkins)
            txt.enabled =
                true // similar to the console output, contains issue signature to manually edit baseline files
        }
    }

//    java {
//        withSourcesJar()
//        withJavadocJar()
//    }
}


tasks.register<JacocoReport>("generateMergedReport") {
    //dependsOn(subprojects.test)
    dependsOn(subprojects.map { it.getTasksByName("test", false) })
    additionalSourceDirs.setFrom(files(subprojects.map { it.sourceSets.asMap["main"]?.allSource?.srcDirs }))
    sourceDirectories.setFrom(files(subprojects.map { it.sourceSets.asMap["main"]?.allSource?.srcDirs }))
    classDirectories.setFrom(files(subprojects.map { it.sourceSets.asMap["main"]?.output }))
    //line below if fishy
    executionData.setFrom(project.fileTree(Pair("dir", "."), Pair("include", "**/build/jacoco/test.exec")))
//    sourceDirectories.setFrom files(subprojects.sourceSets.main.allSource.srcDirs)
//    classDirectories.setFrom files(subprojects.sourceSets.main.output)
//    executionData.setFrom project.fileTree(dir = ".", include = "**/build/jacoco/test.exec")
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.isEnabled = true
    }
}

allprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


val publications: PublicationContainer = (extensions.getByName("publishing") as PublishingExtension).publications

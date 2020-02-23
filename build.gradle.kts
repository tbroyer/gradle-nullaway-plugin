import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

group = "net.ltgt.gradle"

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

tasks.withType<KotlinCompile>().configureEach {
    // This is the version used in Gradle 5.2, for backwards compatibility when we'll upgrade
    kotlinOptions.apiVersion = "1.3"

    kotlinOptions.allWarningsAsErrors = true
}

gradle.taskGraph.whenReady {
    if (hasTask(":publishPlugins")) {
        check("git diff --quiet --exit-code".execute(null, rootDir).waitFor() == 0) { "Working tree is dirty" }
        val process = "git describe --exact-match".execute(null, rootDir)
        check(process.waitFor() == 0) { "Version is not tagged" }
        version = process.text.trim().removePrefix("v")
    }
}

// See https://github.com/gradle/gradle/issues/7974
val additionalPluginClasspath by configurations.creating

val errorpronePluginVersion = "1.0.0"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}
dependencies {
    compileOnly("net.ltgt.gradle:gradle-errorprone-plugin:$errorpronePluginVersion")
    testImplementation("net.ltgt.gradle:gradle-errorprone-plugin:$errorpronePluginVersion")

    testImplementation("com.google.truth:truth:1.0.1") {
        // See https://github.com/google/truth/issues/333
        exclude(group = "junit", module = "junit")
    }
    testRuntimeOnly("junit:junit:4.13") {
        // See https://github.com/google/truth/issues/333
        because("Truth needs it")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")

    additionalPluginClasspath("net.ltgt.gradle:gradle-errorprone-plugin:$errorpronePluginVersion")
    additionalPluginClasspath("com.android.tools.build:gradle:3.5.3")
}

tasks {
    pluginUnderTestMetadata {
        this.pluginClasspath.from(additionalPluginClasspath)
    }
    test {
        val testGradleVersion = project.findProperty("test.gradle-version")
        testGradleVersion?.also { systemProperty("test.gradle-version", testGradleVersion) }

        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

gradlePlugin {
    plugins {
        register("nullaway") {
            id = "net.ltgt.nullaway"
            displayName = "Adds NullAway DSL to Gradle Error Prone plugin"
            description = "Adds NullAway DSL to Gradle Error Prone plugin"
            implementationClass = "net.ltgt.gradle.nullaway.NullAwayPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/tbroyer/gradle-nullaway-plugin"
    vcsUrl = "https://github.com/tbroyer/gradle-nullaway-plugin"
    tags = listOf("javac", "error-prone", "nullaway", "nullability")

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
    }
}

ktlint {
    version.set("0.36.0")
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
}

fun String.execute(envp: Array<String>?, workingDir: File?) =
    Runtime.getRuntime().exec(this, envp, workingDir)

val Process.text: String
    get() = inputStream.bufferedReader().readText()

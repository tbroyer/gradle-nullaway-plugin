import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.4.2"
    id("com.android.lint") version "7.4.2"
    id("org.nosphere.gradle.github.actions") version "1.3.2"
}

group = "net.ltgt.gradle"

// Make sure Gradle Module Metadata targets the appropriate JVM version
tasks.withType<JavaCompile>().configureEach {
    options.release.set(kotlinDslPluginOptions.jvmTarget.map { JavaVersion.toVersion(it).majorVersion.toInt() })
}

tasks.withType<KotlinCompile>().configureEach {
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

val errorpronePluginVersion = "3.1.0"
val errorproneVersion = "2.10.0"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal {
        content {
            includeGroupByRegex("""net\.ltgt\..*""")
        }
    }
}
dependencies {
    compileOnly("net.ltgt.gradle:gradle-errorprone-plugin:$errorpronePluginVersion")
    testImplementation("net.ltgt.gradle:gradle-errorprone-plugin:$errorpronePluginVersion")

    testImplementation("com.google.truth.extensions:truth-java8-extension:1.1.5") {
        // See https://github.com/google/truth/issues/333
        exclude(group = "junit", module = "junit")
    }
    testRuntimeOnly("junit:junit:4.13.2") {
        // See https://github.com/google/truth/issues/333
        because("Truth needs it")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")

    testImplementation("com.google.errorprone:error_prone_check_api:$errorproneVersion") {
        exclude(group = "com.google.errorprone", module = "javac")
    }

    additionalPluginClasspath("net.ltgt.gradle:gradle-errorprone-plugin:$errorpronePluginVersion")
}

tasks {
    pluginUnderTestMetadata {
        this.pluginClasspath.from(additionalPluginClasspath)
    }
    test {
        val testJavaToolchain = project.findProperty("test.java-toolchain")
        testJavaToolchain?.also {
            val metadata = project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(testJavaToolchain.toString()))
            }.get().metadata
            systemProperty("test.java-version", metadata.languageVersion.asInt())
            systemProperty("test.java-home", metadata.installationPath.asFile.canonicalPath)
        }

        val testGradleVersion = project.findProperty("test.gradle-version")
        testGradleVersion?.also { systemProperty("test.gradle-version", testGradleVersion) }

        systemProperty("errorprone.version", errorproneVersion)

        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

gradlePlugin {
    website.set("https://github.com/tbroyer/gradle-nullaway-plugin")
    vcsUrl.set("https://github.com/tbroyer/gradle-nullaway-plugin")
    plugins {
        register("nullaway") {
            id = "net.ltgt.nullaway"
            displayName = "Adds NullAway DSL to Gradle Error Prone plugin"
            description = "Adds NullAway DSL to Gradle Error Prone plugin"
            implementationClass = "net.ltgt.gradle.nullaway.NullAwayPlugin"
            tags.addAll("javac", "error-prone", "nullaway", "nullability")
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Adds NullAway DSL to Gradle Error Prone plugin")
            description.set("Adds NullAway DSL to Gradle Error Prone plugin")
            url.set("https://github.com/tbroyer/gradle-nullaway-plugin")
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            developers {
                developer {
                    name.set("Thomas Broyer")
                    email.set("t.broyer@ltgt.net")
                }
            }
            scm {
                connection.set("https://github.com/tbroyer/gradle-nullaway-plugin.git")
                developerConnection.set("scm:git:ssh://github.com:tbroyer/gradle-nullaway-plugin.git")
                url.set("https://github.com/tbroyer/gradle-nullaway-plugin")
            }
        }
    }
}

ktlint {
    version.set("0.49.1")
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
}

fun String.execute(envp: Array<String>?, workingDir: File?) =
    Runtime.getRuntime().exec(this, envp, workingDir)

val Process.text: String
    get() = inputStream.bufferedReader().readText()

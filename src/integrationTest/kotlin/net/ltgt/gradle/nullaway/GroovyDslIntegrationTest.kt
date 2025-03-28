package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Properties

class GroovyDslIntegrationTest {
    @TempDir
    lateinit var testProjectDir: File
    lateinit var settingsFile: File
    lateinit var buildFile: File

    @BeforeEach
    fun setupProject() {
        assumeCompatibleGradleAndJavaVersions()

        testProjectDir.resolve("gradle.properties").outputStream().use {
            Properties().apply {
                setProperty("org.gradle.java.home", testJavaHome)
                store(it, null)
            }
        }
        settingsFile =
            testProjectDir.resolve("settings.gradle").apply {
                createNewFile()
            }
        buildFile =
            testProjectDir.resolve("build.gradle").apply {
                writeText(
                    """
                    import net.ltgt.gradle.errorprone.CheckSeverity

                    plugins {
                        id("java-library")
                        id("net.ltgt.errorprone")
                        id("net.ltgt.nullaway")
                    }

                    repositories {
                        mavenCentral()
                    }
                    dependencies {
                        errorprone "com.google.errorprone:error_prone_core:$errorproneVersion"
                        errorprone "com.uber.nullaway:nullaway:$nullawayVersion"
                    }

                    tasks.withType(JavaCompile).configureEach {
                        options.compilerArgs.add("-Werror")
                    }

                    nullaway {
                        annotatedPackages.add("test")
                    }
                    """.trimIndent(),
                )
            }
        if (testGradleVersion < GradleVersion.version("7.0")) {
            buildFile.appendText(
                """

                allprojects {
                    configurations.all {
                        attributes.attribute(Attribute.of("org.gradle.jvm.environment", String), "standard-jvm")
                    }
                }
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `can disable nullaway`() {
        // given
        buildFile.appendText(
            """

            tasks.withType(JavaCompile).configureEach {
                options.errorprone.nullaway.disable()
            }
            """.trimIndent(),
        )
        testProjectDir.writeFailureSource()

        // when
        val result = testProjectDir.buildWithArgs(":compileJava")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `can configure nullaway`() {
        // given
        buildFile.appendText(
            """
            
            tasks.withType(JavaCompile).configureEach {
                options.errorprone.nullaway {
                    severity = CheckSeverity.DEFAULT
                    ${if (nullawaySupportsOnlyNullMarked) "onlyNullMarked = false" else ""}
                    annotatedPackages = project.nullaway.annotatedPackages
                    unannotatedSubPackages = ["test.dummy"]
                    unannotatedClasses = ["test.Unannotated"]
                    knownInitializers = ["com.foo.Bar.method"]
                    excludedClassAnnotations = ["com.example.NullAwayExcluded"]
                    excludedClasses = ["test.Excluded"]
                    excludedFieldAnnotations = ["javax.ws.rs.core.Context"]
                    customInitializerAnnotations = ["com.foo.Initializer"]
                    externalInitAnnotations = ["com.example.ExternalInit"]
                    treatGeneratedAsUnannotated = true
                    acknowledgeRestrictiveAnnotations = true
                    checkOptionalEmptiness = true
                    suggestSuppressions = true
                    assertsEnabled = true
                    exhaustiveOverride = true
                    castToNonNullMethod = "com.foo.Bar.castToNonNull"
                    checkOptionalEmptinessCustomClasses = ["com.foo.Optional"]
                    autoFixSuppressionComment = "Auto-fix\\u0020suppression"
                    handleTestAssertionLibraries = true
                    acknowledgeAndroidRecent = true
                    checkContracts = true
                    customContractAnnotations = ["com.example.Contract"]
                    customNullableAnnotations = ["com.example.CouldBeNull"]
                    customNonnullAnnotations = ["com.example.MustNotBeNull"]
                    customGeneratedCodeAnnotations = ["com.example.Generated"]
                    jspecifyMode = true
                    extraFuturesClasses = ["com.example.Future"]
                }
            }
            """.trimIndent(),
        )
        testProjectDir.writeSuccessSource()

        // when
        val result = testProjectDir.buildWithArgs(":compileJava")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

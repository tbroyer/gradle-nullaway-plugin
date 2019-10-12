package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import java.io.File
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GroovyDslIntegrationTest {
    @TempDir
    lateinit var testProjectDir: File
    lateinit var settingsFile: File
    lateinit var buildFile: File

    @BeforeEach
    fun setupProject() {
        settingsFile = testProjectDir.resolve("settings.gradle").apply {
            createNewFile()
        }
        buildFile = testProjectDir.resolve("build.gradle").apply {
            writeText(
                """
                import net.ltgt.gradle.errorprone.CheckSeverity

                plugins {
                    id("java-library")
                    id("${ErrorPronePlugin.PLUGIN_ID}")
                    id("${NullAwayPlugin.PLUGIN_ID}")
                }

                repositories {
                    mavenCentral()
                }
                dependencies {
                    errorprone "com.google.errorprone:error_prone_core:$errorproneVersion"
                    errorprone "com.uber.nullaway:nullaway:$nullawayVersion"
                    errorproneJavac "com.google.errorprone:javac:$errorproneJavacVersion"
                }

                tasks.withType(JavaCompile).configureEach {
                    options.compilerArgs.add("-Werror")
                }

                nullaway {
                    annotatedPackages.add("test")
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `can disable nullaway`() {
        // given
        buildFile.appendText(
            """

            tasks.withType(JavaCompile).configureEach {
                options.errorprone.nullaway.severity = CheckSeverity.OFF
            }
            """.trimIndent()
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
                }
            }
            """.trimIndent()
        )
        testProjectDir.writeSuccessSource()

        // when
        val result = testProjectDir.buildWithArgs(":compileJava")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

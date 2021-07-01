package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.TruthJUnit.assume
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractPluginIntegrationTest(
    private val buildFileContent: String,
    private val compileTaskName: String
) {

    @TempDir
    lateinit var testProjectDir: File
    lateinit var settingsFile: File
    lateinit var buildFile: File

    @BeforeEach
    fun setupProject() {
        settingsFile = testProjectDir.resolve("settings.gradle.kts").apply {
            createNewFile()
        }
        buildFile = testProjectDir.resolve("build.gradle.kts").apply {
            writeText(
                """
                import net.ltgt.gradle.errorprone.*
                import net.ltgt.gradle.nullaway.nullaway

                $buildFileContent

                repositories {
                    mavenCentral()
                }
                dependencies {
                    errorprone("com.google.errorprone:error_prone_core:$errorproneVersion")
                    errorprone("com.uber.nullaway:nullaway:$nullawayVersion")
                    errorproneJavac("com.google.errorprone:javac:$errorproneJavacVersion")
                }

                tasks.withType<JavaCompile>().configureEach {
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
    fun `missing annotated packages option`() {
        // given
        buildFile.appendText(
            """

            nullaway {
                annotatedPackages.empty()
            }
            """.trimIndent()
        )
        testProjectDir.writeSuccessSource()

        // when
        val result = testProjectDir.buildWithArgsAndFail(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("Must specify annotated packages, using the -XepOpt:NullAway:AnnotatedPackages=[...] flag.")
    }

    @Test
    fun `compilation succeeds`() {
        // given
        testProjectDir.writeSuccessSource()

        // when
        val result = testProjectDir.buildWithArgs(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `compilation fails`() {
        // given
        testProjectDir.writeFailureSource()

        // when
        val result = testProjectDir.buildWithArgsAndFail(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains(FAILURE_SOURCE_COMPILATION_ERROR)
    }

    @Test
    fun `can disable nullaway`() {
        // given
        buildFile.appendText(
            """

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone.nullaway.severity.set(CheckSeverity.OFF)
            }
            """.trimIndent()
        )
        testProjectDir.writeFailureSource()

        // when
        val result = testProjectDir.buildWithArgs(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `can configure nullaway`() {
        // given
        buildFile.appendText(
            """

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone.nullaway {
                    unannotatedSubPackages.add("test.dummy")
                    unannotatedClasses.add("test.Unannotated")
                    knownInitializers.add("com.foo.Bar.method")
                    excludedClassAnnotations.add("com.example.NullAwayExcluded")
                    excludedClasses.add("test.Excluded")
                    excludedFieldAnnotations.add("javax.ws.rs.core.Context")
                    customInitializerAnnotations.add("com.foo.Initializer")
                    externalInitAnnotations.add("com.example.ExternalInit")
                    treatGeneratedAsUnannotated.set(true)
                    acknowledgeRestrictiveAnnotations.set(true)
                    checkOptionalEmptiness.set(true)
                    suggestSuppressions.set(true)
                    isAssertsEnabled.set(true)
                    isExhaustiveOverride.set(true)
                    castToNonNullMethod.set("com.foo.Bar.castToNonNull")
                    checkOptionalEmptinessCustomClasses.add("com.foo.Optional")
                    autoFixSuppressionComment.set("Auto-fix\\u0020suppression")
                    handleTestAssertionLibraries.set(true)
                    acknowledgeAndroidRecent.set(true)
                    checkContracts.set(true)
                    customContractAnnotations.add("com.example.Contract")
                }
            }
            """.trimIndent()
        )
        testProjectDir.writeSuccessSource()

        // when
        val result = testProjectDir.buildWithArgs(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `plays nicely with up-to-date checks`() {
        // given
        buildFile.appendText(
            """

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone.nullaway {
                    if (project.hasProperty("disable-nullaway")) {
                        severity.set(CheckSeverity.OFF)
                    }
                    autoFixSuppressionComment.set(project.property("autofix-comment") as String)
                }
            }
            """.trimIndent()
        )
        testProjectDir.writeSuccessSource()

        // when
        testProjectDir.buildWithArgs(compileTaskName, "-Pautofix-comment=foo").also { result ->
            // then
            assume().that(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        }

        // when
        testProjectDir.buildWithArgs(compileTaskName, "-Pautofix-comment=bar").also { result ->
            // then
            // (specifically, we don't want UP_TO_DATE)
            assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        }

        // when
        testProjectDir.buildWithArgs(compileTaskName, "-Pdisable-nullaway", "-Pautofix-comment=bar").also { result ->
            // then
            // (specifically, we don't want UP_TO_DATE)
            assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        }

        // when
        testProjectDir.buildWithArgs(compileTaskName, "-Pdisable-nullaway", "-Pautofix-comment=baz").also { result ->
            // then
            // Changing a property while the check is disabled has no impact on up-to-date checks
            assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
        }
    }
}

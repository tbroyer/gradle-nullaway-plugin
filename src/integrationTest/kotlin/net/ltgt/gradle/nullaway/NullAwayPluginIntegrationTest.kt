package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.TruthJUnit.assume
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Properties

class NullAwayPluginIntegrationTest {
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
            testProjectDir.resolve("settings.gradle.kts").apply {
                createNewFile()
            }
        buildFile =
            testProjectDir.resolve("build.gradle.kts").apply {
                writeText(
                    """
                    import net.ltgt.gradle.errorprone.*
                    import net.ltgt.gradle.nullaway.nullaway

                    plugins {
                        `java-library`
                        id("net.ltgt.errorprone")
                        id("net.ltgt.nullaway")
                    }

                    repositories {
                        mavenCentral()
                    }
                    dependencies {
                        errorprone("com.google.errorprone:error_prone_core:$errorproneVersion")
                        errorprone("com.uber.nullaway:nullaway:$nullawayVersion")
                    }

                    tasks.withType<JavaCompile>().configureEach {
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
                        attributes.attribute(Attribute.of("org.gradle.jvm.environment", String::class.java), "standard-jvm")
                    }
                }
                """.trimIndent(),
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
            """.trimIndent(),
        )
        testProjectDir.writeSuccessSource()

        // when
        val result = testProjectDir.buildWithArgsAndFail(":compileJava")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("Must specify annotated packages, using the -XepOpt:NullAway:AnnotatedPackages=[...] flag.")
    }

    @Test
    fun `compilation succeeds`() {
        // given
        testProjectDir.writeSuccessSource()

        // when
        val result = testProjectDir.buildWithArgs(":compileJava")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `compilation fails`() {
        // given
        testProjectDir.writeFailureSource()

        // when
        val result = testProjectDir.buildWithArgsAndFail(":compileJava")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains(FAILURE_SOURCE_COMPILATION_ERROR)
    }

    @Test
    fun `can disable nullaway`() {
        // given
        buildFile.appendText(
            """

            tasks.withType<JavaCompile>().configureEach {
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

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone.nullaway {
                    severity.set(CheckSeverity.DEFAULT)
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
                    customNullableAnnotations.add("com.example.CouldBeNull")
                    customNonnullAnnotations.add("com.example.MustNotBeNull")
                    isJSpecifyMode.set(true)
                    extraFuturesClasses.add("com.example.Future")
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

    @Test
    fun `plays nicely with up-to-date checks`() {
        // given
        buildFile.appendText(
            """

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone.nullaway {
                    if (project.hasProperty("disable-nullaway")) {
                        disable()
                    }
                    autoFixSuppressionComment.set(project.property("autofix-comment") as String)
                }
            }
            """.trimIndent(),
        )
        testProjectDir.writeSuccessSource()

        // when
        testProjectDir.buildWithArgs(":compileJava", "-Pautofix-comment=foo").also { result ->
            // then
            assume().that(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        }

        // when
        testProjectDir.buildWithArgs(":compileJava", "-Pautofix-comment=bar").also { result ->
            // then
            // (specifically, we don't want UP_TO_DATE)
            assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        }

        // when
        testProjectDir.buildWithArgs(":compileJava", "-Pdisable-nullaway", "-Pautofix-comment=bar").also { result ->
            // then
            // (specifically, we don't want UP_TO_DATE)
            assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        }

        // when
        testProjectDir.buildWithArgs(":compileJava", "-Pdisable-nullaway", "-Pautofix-comment=baz").also { result ->
            // then
            // Changing a property while the check is disabled has no impact on up-to-date checks
            assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
        }
    }

    @Test
    fun `is configuration-cache friendly`() {
        // given
        testProjectDir.writeSuccessSource()

        // Prime the configuration cache
        testProjectDir.buildWithArgs("--configuration-cache", "compileJava")

        // when
        val result = testProjectDir.buildWithArgs("--configuration-cache", "compileJava")

        // then
        assertThat(result.output).contains("Reusing configuration cache.")
    }
}

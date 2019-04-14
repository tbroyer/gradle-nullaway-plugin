package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractPluginIntegrationTest(
    private val buildFileContent: String,
    private val compileTaskName: String
) {
    companion object {
        internal val testGradleVersion = System.getProperty("test.gradle-version", GradleVersion.current().version)

        internal const val errorproneVersion = "2.3.3"
        internal const val errorproneJavacVersion = "9+181-r4173-1"
        internal const val nullawayVersion = "0.7.2"

        internal const val FAILURE_SOURCE_COMPILATION_ERROR = "Failure.java:8: warning: [NullAway]"
    }

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
            writeText("""
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
            """.trimIndent())
        }
    }

    protected fun writeSuccessSource() {
        File(testProjectDir.resolve("src/main/java/test").apply { mkdirs() }, "Success.java").apply {
            createNewFile()
            writeText("""
                package test;

                public class Success {
                    static void log(@Nullable Object x) {
                        if (x != null) {
                            System.out.println(x.toString());
                        }
                    }
                    static void foo() {
                        log(null);
                    }
                }

                @interface Nullable {}
            """.trimIndent())
        }
    }

    protected fun writeFailureSource() {
        File(testProjectDir.resolve("src/main/java/test").apply { mkdirs() }, "Failure.java").apply {
            createNewFile()
            writeText("""
                package test;

                public class Failure {
                    static void log(Object x) {
                        System.out.println(x.toString());
                    }
                    static void foo() {
                        log(null);
                    }
                }
            """.trimIndent())
        }
    }

    protected fun buildWithArgs(vararg tasks: String): BuildResult {
        return prepareBuild(*tasks)
            .build()
    }

    protected fun buildWithArgsAndFail(vararg tasks: String): BuildResult {
        return prepareBuild(*tasks)
            .buildAndFail()
    }

    private fun prepareBuild(vararg tasks: String): GradleRunner {
        return GradleRunner.create()
            .withGradleVersion(testGradleVersion)
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments(*tasks)
    }

    @Test
    fun `missing annotated packages option`() {
        // given
        buildFile.appendText("""

            nullaway {
                annotatedPackages.empty()
            }
        """.trimIndent())
        writeSuccessSource()

        // when
        val result = buildWithArgsAndFail(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("Must specify annotated packages, using the -XepOpt:NullAway:AnnotatedPackages=[...] flag.")
    }

    @Test
    fun `compilation succeeds`() {
        // given
        writeSuccessSource()

        // when
        val result = buildWithArgs(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `compilation fails`() {
        // given
        writeFailureSource()

        // when
        val result = buildWithArgsAndFail(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains(FAILURE_SOURCE_COMPILATION_ERROR)
    }

    @Test
    fun `can disable nullaway`() {
        // given
        buildFile.appendText("""

            tasks.withType<JavaCompile>().configureEach {
                options.errorprone.nullaway.severity.set(CheckSeverity.OFF)
            }
        """.trimIndent())
        writeFailureSource()

        // when
        val result = buildWithArgs(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `can configure nullaway`() {
        // given
        buildFile.appendText("""

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
                }
            }
        """.trimIndent())
        writeSuccessSource()

        // when
        val result = buildWithArgs(compileTaskName)

        // then
        assertThat(result.task(compileTaskName)?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}

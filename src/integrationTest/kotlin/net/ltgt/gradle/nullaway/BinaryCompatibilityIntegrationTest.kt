package net.ltgt.gradle.nullaway

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Properties

class BinaryCompatibilityIntegrationTest {
    @TempDir
    lateinit var testProjectDir: File
    lateinit var settingsFile: File
    lateinit var buildFile: File

    @TempDir
    lateinit var conventionPluginProjectDir: File
    lateinit var conventionPluginBuildFile: File

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
                    plugins {
                        `java-library`
                        id("net.ltgt.errorprone")
                        id("net.ltgt.nullaway")
                        id("convention.nullaway")
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
        testProjectDir.resolve("gradle.properties").copyTo(conventionPluginProjectDir.resolve("gradle.properties"))
        conventionPluginProjectDir.resolve("settings.gradle.kts").apply {
            writeText(
                """
                rootProject.name = "convention-plugin"
                """.trimIndent(),
            )
        }
        conventionPluginBuildFile =
            conventionPluginProjectDir.resolve("build.gradle.kts").apply {
                createNewFile()
            }
    }

    private fun buildConventionPlugin() {
        GradleRunner
            .create()
            .withGradleVersion(testGradleVersion.version)
            .withProjectDir(conventionPluginProjectDir)
            .withArguments("assemble")
            .build()
    }

    private fun buildProject(vararg tasks: String) =
        GradleRunner
            .create()
            .withGradleVersion(testGradleVersion.version)
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .apply {
                withPluginClasspath(pluginClasspath + conventionPluginProjectDir.resolve("build/libs/convention-plugin.jar"))
            }.withArguments(*tasks)
            .build()

    @Test
    fun `is binary-compatible with previous versions (kotlin-dsl flavor)`() {
        // given
        conventionPluginBuildFile.writeText(
            """
            plugins {
                `kotlin-dsl`
            }

            repositories {
                gradlePluginPortal()
            }

            dependencies {
                implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.4.0")
                implementation("net.ltgt.nullaway:net.ltgt.nullaway.gradle.plugin:2.4.1")
            }
            """.trimIndent(),
        )
        conventionPluginProjectDir.resolve("src/main/kotlin").apply { mkdirs() }.resolve("convention.nullaway.gradle.kts").apply {
            writeText(
                """
                import net.ltgt.gradle.errorprone.*
                import net.ltgt.gradle.nullaway.*

                plugins {
                    id("net.ltgt.errorprone")
                    id("net.ltgt.nullaway")
                }

                configure<NullAwayExtension> {
                    annotatedPackages.add("test")
                }

                tasks.withType<JavaCompile>().configureEach {
                    options.errorprone.nullaway {
                        isAssertsEnabled.set(true)
                        isExhaustiveOverride.set(true)
                        isJSpecifyMode.set(true)
                    }
                }
                """.trimIndent(),
            )
        }

        buildConventionPlugin()

        testProjectDir.writeSuccessSource()

        // when
        val result = buildProject(":compileJava", "--stacktrace")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `is binary-compatible with previous versions (java flavor)`() {
        // given
        conventionPluginBuildFile.writeText(
            """
            plugins {
                `java-gradle-plugin`
            }

            repositories {
                gradlePluginPortal()
            }

            dependencies {
                implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:4.4.0")
                implementation("net.ltgt.nullaway:net.ltgt.nullaway.gradle.plugin:2.4.1")
            }
            
            gradlePlugin {
                plugins {
                    create("conventionPlugin") {
                        id = "convention.nullaway"
                        implementationClass = "convention.ConventionNullAwayPlugin"
                    }
                }
            }
            """.trimIndent(),
        )
        conventionPluginProjectDir.resolve("src/main/java/convention").apply { mkdirs() }.resolve("ConventionNullAwayPlugin.java").apply {
            writeText(
                """
                package convention;

                import net.ltgt.gradle.errorprone.*;
                import net.ltgt.gradle.nullaway.*;
                import org.gradle.api.Plugin;
                import org.gradle.api.Project;
                import org.gradle.api.plugins.ExtensionAware;
                import org.gradle.api.tasks.compile.JavaCompile;
                
                public class ConventionNullAwayPlugin implements Plugin<Project> {
                  @Override
                  public void apply(Project project) {
                    project.getPluginManager().apply("net.ltgt.errorprone");
                    project.getPluginManager().apply("net.ltgt.nullaway");
                
                    project
                        .getExtensions()
                        .configure(
                            NullAwayExtension.class,
                            nullaway -> {
                              nullaway.getAnnotatedPackages().add("test");
                            });
                
                    project
                        .getTasks()
                        .withType(JavaCompile.class)
                        .configureEach(
                            task -> {
                              ErrorProneOptions errorproneOptions =
                                  ((ExtensionAware) task.getOptions())
                                      .getExtensions()
                                      .getByType(ErrorProneOptions.class);
                              NullAwayOptions nullawayOptions =
                                  ((ExtensionAware) errorproneOptions)
                                      .getExtensions()
                                      .getByType(NullAwayOptions.class);
                              nullawayOptions.getAssertsEnabled().set(true);
                              nullawayOptions.getExhaustiveOverride().set(true);
                              nullawayOptions.getJspecifyMode().set(true);
                            });
                  }
                }
                """.trimIndent(),
            )
        }

        buildConventionPlugin()

        testProjectDir.writeSuccessSource()

        // when
        val result = buildProject(":compileJava", "--stacktrace")

        // then
        assertThat(result.task(":compileJava")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
